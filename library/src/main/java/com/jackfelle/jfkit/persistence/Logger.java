//
//	The MIT License (MIT)
//
//	Copyright © 2017-2019 Jacopo Filié
//
//	Permission is hereby granted, free of charge, to any person obtaining a copy
//	of this software and associated documentation files (the "Software"), to deal
//	in the Software without restriction, including without limitation the rights
//	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//	copies of the Software, and to permit persons to whom the Software is
//	furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in all
//	copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//	SOFTWARE.
//

package com.jackfelle.jfkit.persistence;

import android.content.Context;
import android.util.Log;

import com.jackfelle.jfkit.BuildConfig;
import com.jackfelle.jfkit.data.Strings;
import com.jackfelle.jfkit.utilities.ObserversController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;

public class Logger
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	public static final @NonNull String FORMAT_DATE = "%1$@";
	public static final @NonNull String FORMAT_MESSAGE = "%2$@";
	public static final @NonNull String FORMAT_PROCESS_ID = "%3$@";
	public static final @NonNull String FORMAT_SEVERITY = "%4$@";
	public static final @NonNull String FORMAT_THREAD_ID = "%5$@";
	public static final @NonNull String FORMAT_TIME = "%6$@";
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Types
	
	public enum Output
	{
		CONSOLE(1 << 0),
		DELEGATES(1 << 1),
		FILE(1 << 2);
		
		public static @NonNull EnumSet<Output> ALL = EnumSet.allOf(Output.class);
		
		private int _value;
		
		Output(int value)
		{
			_value = value;
		}
		
		public int getValue()
		{
			return _value;
		}
	}
	
	public enum Rotation
	{
		NONE(0),
		HOUR(1),
		DAY(2),
		WEEK(3),
		MONTH(4);
		
		private int _value;
		
		Rotation(int value)
		{
			_value = value;
		}
		
		public int getValue()
		{
			return _value;
		}
	}
	
	public enum Severity
	{
		EMERGENCY(0),
		ALERT(1),
		CRITICAL(2),
		ERROR(3),
		WARNING(4),
		NOTICE(5),
		INFO(6),
		DEBUG(7);
		
		private int _value;
		
		Severity(int value)
		{
			_value = value;
		}
		
		public int getValue()
		{
			return _value;
		}
		
		public static @NonNull String stringFromSeverity(Severity severity)
		{
			switch(severity)
			{
				case ALERT:
					return "Alert";
				case CRITICAL:
					return "Critical";
				case DEBUG:
					return "Debug";
				case EMERGENCY:
					return "Emergency";
				case ERROR:
					return "Error";
				case INFO:
					return "Info";
				case NOTICE:
					return "Notice";
				case WARNING:
					return "Warning";
				default:
					return "";
			}
		}
	}
	
	public enum Tags
	{
		ATTENTION(1 << 0),
		CLUE(1 << 1),
		COMMENT(1 << 2),
		CRITICAL(1 << 3),
		DEVELOPER(1 << 4),
		ERROR(1 << 5),
		FILE_SYSTEM(1 << 6),
		HARDWARE(1 << 7),
		MARKER(1 << 8),
		NETWORK(1 << 9),
		SECURITY(1 << 10),
		SYSTEM(1 << 11),
		USER(1 << 12);
		
		public static @NonNull EnumSet<Tags> NONE = EnumSet.noneOf(Tags.class);
		
		private int _value;
		
		Tags(int value)
		{
			_value = value;
		}
		
		public int getValue()
		{
			return _value;
		}
		
		public static @NonNull String stringFromTags(EnumSet<Tags> tags)
		{
			int size = tags.size();
			if(size == 0)
				return "";
			
			List<String> tagStrings = new ArrayList<>(size);
			
			if(tags.contains(ATTENTION))
				tagStrings.add("#Attention");
			if(tags.contains(CLUE))
				tagStrings.add("#Clue");
			if(tags.contains(COMMENT))
				tagStrings.add("#Comment");
			if(tags.contains(CRITICAL))
				tagStrings.add("#Critical");
			if(tags.contains(DEVELOPER))
				tagStrings.add("#Developer");
			if(tags.contains(ERROR))
				tagStrings.add("#Error");
			if(tags.contains(FILE_SYSTEM))
				tagStrings.add("#FileSystem");
			if(tags.contains(HARDWARE))
				tagStrings.add("#Hardware");
			if(tags.contains(MARKER))
				tagStrings.add("#Marker");
			if(tags.contains(NETWORK))
				tagStrings.add("#Network");
			if(tags.contains(SECURITY))
				tagStrings.add("#Security");
			if(tags.contains(SYSTEM))
				tagStrings.add("#System");
			if(tags.contains(USER))
				tagStrings.add("#User");
			
			StringBuilder builder = new StringBuilder(tagStrings.get(0));
			for(int i = 1; i < tagStrings.size(); i++)
			{
				builder.append(" ");
				builder.append(tagStrings.get(i));
			}
			return builder.toString();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Delegate
	{
		void logMessage(@NonNull Logger sender, @NonNull String message, @NonNull Date date);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private WeakReference<Context> _context;
	private DateFormat _dateFormat;
	private String _format;
	private EnumSet<Output> _outputFilter;
	private List<String> _requestedFormatValues;
	private Severity _severityFilter;
	private DateFormat _timeFormat;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - File system
	
	private String _fileName;
	private Rotation _rotation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private @NonNull ObserversController<Delegate> _delegatesController;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Data
	
	public static @NonNull File getDefaultDirectory(@NonNull Context context)
	{
		return context.getApplicationContext().getDir("Logs", Context.MODE_PRIVATE);
	}
	
	public Context getContext()
	{
		synchronized(this)
		{
			return ((_context != null) ? _context.get() : null);
		}
	}
	
	public void setContext(Context context)
	{
		synchronized(this)
		{
			WeakReference<Context> reference = null;
			if(context != null)
			{
				context = context.getApplicationContext();
				if(context != null)
					reference = new WeakReference<>(context);
			}
			_context = reference;
		}
	}
	
	public @NonNull DateFormat getDateFormat()
	{
		synchronized(this)
		{
			if(_dateFormat == null)
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getDefault());
				_dateFormat = dateFormat;
			}
			return _dateFormat;
		}
	}
	
	public void setDateFormat(DateFormat dateFormat)
	{
		synchronized(this)
		{
			_dateFormat = dateFormat;
		}
	}
	
	public @NonNull String getFormat()
	{
		synchronized(this)
		{
			if(_format == null)
				_format = String.format(Locale.US, "%s %s [%s:%s] %s", FORMAT_DATE, FORMAT_TIME, FORMAT_PROCESS_ID, FORMAT_THREAD_ID, FORMAT_MESSAGE);
			return _format;
		}
	}
	
	public void setFormat(String format)
	{
		synchronized(this)
		{
			_format = format;
			this.setRequestedFormatValues(null);
		}
	}
	
	public @NonNull EnumSet<Output> getOutputFilter()
	{
		synchronized(this)
		{
			return _outputFilter;
		}
	}
	
	public void setOutputFilter(@NonNull EnumSet<Output> outputFilter)
	{
		synchronized(this)
		{
			_outputFilter = outputFilter;
		}
	}
	
	private @NonNull List<String> getRequestedFormatValues()
	{
		synchronized(this)
		{
			if(_requestedFormatValues == null)
			{
				String[] possibleValues = new String[] {FORMAT_DATE, FORMAT_MESSAGE, FORMAT_PROCESS_ID, FORMAT_SEVERITY, FORMAT_THREAD_ID, FORMAT_TIME};
				
				String format = this.getFormat();
				
				List<String> requestedValues = new ArrayList<>(possibleValues.length);
				for(String value : possibleValues)
				{
					if(format.contains(value))
						requestedValues.add(value);
				}
				
				_requestedFormatValues = Collections.unmodifiableList(requestedValues);
			}
			return _requestedFormatValues;
		}
	}
	
	private void setRequestedFormatValues(List<String> requestedFormatValues)
	{
		synchronized(this)
		{
			_requestedFormatValues = requestedFormatValues;
		}
	}
	
	public @NonNull Severity getSeverityFilter()
	{
		synchronized(this)
		{
			return _severityFilter;
		}
	}
	
	public void setSeverityFilter(@NonNull Severity severityFilter)
	{
		synchronized(this)
		{
			_severityFilter = severityFilter;
		}
	}
	
	public @NonNull DateFormat getTimeFormat()
	{
		synchronized(this)
		{
			if(_timeFormat == null)
			{
				DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSSZ", Locale.getDefault());
				timeFormat.setTimeZone(TimeZone.getDefault());
				_timeFormat = timeFormat;
			}
			return _timeFormat;
		}
	}
	
	public void setTimeFormat(DateFormat timeFormat)
	{
		synchronized(this)
		{
			_timeFormat = timeFormat;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - File system
	
	public @NonNull String getFileName()
	{
		synchronized(this)
		{
			if(_fileName == null)
				_fileName = "Log.log";
			return _fileName;
		}
	}
	
	public void setFileName(String fileName)
	{
		synchronized(this)
		{
			_fileName = fileName;
		}
	}
	
	public @NonNull Rotation getRotation()
	{
		synchronized(this)
		{
			return _rotation;
		}
	}
	
	public void setRotation(@NonNull Rotation rotation)
	{
		synchronized(this)
		{
			_rotation = rotation;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Observers
	
	private @NonNull ObserversController<Delegate> getDelegatesController()
	{
		return _delegatesController;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory management
	
	public Logger()
	{
		super();
		
		_delegatesController = new ObserversController<>();
		_outputFilter = Output.ALL;
		_rotation = Rotation.NONE;
		_severityFilter = (BuildConfig.DEBUG ? Severity.DEBUG : Severity.INFO);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data management
	
	public File currentFile()
	{
		Context context = this.getContext();
		return ((context == null) ? null : this.fileForDate(new Date(), context));
	}
	
	private @NonNull String dateStringFromDate(@NonNull Date date)
	{
		DateFormat dateFormat = this.getDateFormat();
		synchronized(dateFormat)
		{
			return dateFormat.format(date);
		}
	}
	
	private @NonNull File fileForDate(@NonNull Date date, @NonNull Context context)
	{
		File folder = Logger.getDefaultDirectory(context);
		String fileName = this.getFileName();
		
		int component = 0;
		boolean shouldAppendSuffix = true;
		
		switch(this.getRotation())
		{
			case HOUR:
			{
				component = Calendar.HOUR_OF_DAY;
				break;
			}
			case DAY:
			{
				component = Calendar.DAY_OF_MONTH;
				break;
			}
			case WEEK:
			{
				component = Calendar.WEEK_OF_MONTH;
				break;
			}
			case MONTH:
			{
				component = Calendar.MONTH;
				break;
			}
			default:
			{
				shouldAppendSuffix = false;
				break;
			}
		}
		
		if(shouldAppendSuffix)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int suffix = calendar.get(component);
			
			String extension = null;
			int index = fileName.lastIndexOf(".");
			if(index > -1)
			{
				extension = fileName.substring(index);
				fileName = fileName.substring(0, index);
			}
			
			fileName = fileName + "-" + suffix;
			if(!Strings.isNullOrEmptyString(extension))
				fileName = fileName + extension;
		}
		
		return new File(folder, fileName);
	}
	
	private @NonNull String timeStringFromDate(@NonNull Date date)
	{
		DateFormat timeFormat = this.getTimeFormat();
		synchronized(timeFormat)
		{
			return timeFormat.format(date);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - File system management
	
	private boolean createFile(@NonNull File file, @NonNull Date currentDate)
	{
		// Checks if the log file exists.
		if(file.exists())
		{
			// Reads the creation date of the existing log file and check if it's still valid. If the file attributes are not readable, it assumes that the log file is still valid.
			Date creationDate = null;
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
			{
				try
				{
					BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
					if(attributes != null)
						creationDate = new Date(attributes.creationTime().toMillis());
				}
				catch(IOException e)
				{
					String tagsString = Tags.stringFromTags(EnumSet.of(Tags.ERROR, Tags.FILE_SYSTEM));
					Log.e("", String.format(Locale.US, "Failed to read attributes of log file at path '%s'. %s", file.getPath(), tagsString), e);
				}
			}
			if(creationDate == null)
				creationDate = new Date(file.lastModified());
			
			if(this.validateFileCreationDate(creationDate, currentDate))
				return true;
			
			if(!file.delete())
				return false;
		}
		
		try
		{
			if(!file.createNewFile())
				return false;
		}
		catch(IOException e)
		{
			String tagsString = Tags.stringFromTags(EnumSet.of(Tags.ERROR, Tags.FILE_SYSTEM));
			Log.e("", String.format(Locale.US, "Failed to create log file at path '%s'. %s", file.getPath(), tagsString), e);
			return false;
		}
		
		return true;
	}
	
	private boolean validateFileCreationDate(@NonNull Date creationDate, @NonNull Date currentDate)
	{
		Calendar creationCalendar = Calendar.getInstance();
		creationCalendar.setTime(creationDate);
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTime(currentDate);
		
		if((creationCalendar.get(Calendar.ERA) != currentCalendar.get(Calendar.ERA)) || (creationCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)))
			return false;
		
		switch(this.getRotation())
		{
			case HOUR:
			{
				if(creationCalendar.get(Calendar.HOUR_OF_DAY) != currentCalendar.get(Calendar.HOUR_OF_DAY))
					return false;
			}
			case DAY:
			{
				if(creationCalendar.get(Calendar.DAY_OF_MONTH) != currentCalendar.get(Calendar.DAY_OF_MONTH))
					return false;
			}
			case WEEK:
			{
				if(creationCalendar.get(Calendar.WEEK_OF_MONTH) != currentCalendar.get(Calendar.WEEK_OF_MONTH))
					return false;
			}
			case MONTH:
			{
				if(creationCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH))
					return false;
			}
			default:
				return true;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers management
	
	public void addDelegate(@NonNull Delegate delegate)
	{
		this.getDelegatesController().addObserver(delegate);
	}
	
	public void removeDelegate(@NonNull Delegate delegate)
	{
		this.getDelegatesController().removeObserver(delegate);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Service management
	
	public void log(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Output> output, @NonNull Severity severity)
	{
		this.log(sender, message, output, severity, Tags.NONE);
	}
	
	public void log(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Output> output, @NonNull Severity severity, @NonNull EnumSet<Tags> tags)
	{
		// Filters by severity
		if(severity.getValue() > this.getSeverityFilter().getValue())
			return;
		
		// Filters by output.
		EnumSet<Output> outputFilter = this.getOutputFilter();
		boolean shouldLogToConsole = ((output.contains(Output.CONSOLE)) && (outputFilter.contains(Output.CONSOLE)));
		boolean shouldLogToDelegates = ((output.contains(Output.DELEGATES)) && (outputFilter.contains(Output.DELEGATES)));
		boolean shouldLogToFile = ((output.contains(Output.FILE)) && (outputFilter.contains(Output.FILE)));
		if(!shouldLogToConsole && !shouldLogToDelegates && !shouldLogToFile)
			return;
		
		// Append tags.
		String tagsString = Tags.stringFromTags(tags);
		if(!Strings.isNullOrEmptyString(tagsString))
			message = message + " " + tagsString;
		
		// Prepares the current date.
		final Date currentDate = new Date();
		
		// Logs to console if needed.
		if(shouldLogToConsole)
		{
			this.logToConsole(sender, message, severity, currentDate);
			if(!shouldLogToDelegates && !shouldLogToFile)
				return;
		}
		
		String format;
		List<String> requestedFormatValues;
		synchronized(this)
		{
			format = this.getFormat();
			requestedFormatValues = this.getRequestedFormatValues();
		}
		
		Map<String, String> values = new HashMap<>(requestedFormatValues.size());
		
		// Converts the severity level to string.
		if(requestedFormatValues.contains(FORMAT_SEVERITY))
			values.put(FORMAT_SEVERITY, Severity.stringFromSeverity(severity));
		
		// Gets the current process ID.
		if(requestedFormatValues.contains(FORMAT_PROCESS_ID))
			values.put(FORMAT_PROCESS_ID, Integer.toString(android.os.Process.myPid()));
		
		// Gets the current thread ID.
		if(requestedFormatValues.contains(FORMAT_THREAD_ID))
			values.put(FORMAT_THREAD_ID, Integer.toString(android.os.Process.myTid()));
		
		// Gets the current date.
		if(requestedFormatValues.contains(FORMAT_DATE))
			values.put(FORMAT_DATE, this.dateStringFromDate(currentDate));
		
		// Gets the current time.
		if(requestedFormatValues.contains(FORMAT_TIME))
			values.put(FORMAT_TIME, this.timeStringFromDate(currentDate));
		
		// Gets the message.
		if(requestedFormatValues.contains(FORMAT_MESSAGE))
			values.put(FORMAT_MESSAGE, message);
		
		// Prepares the log string.
		final String logMessage = Strings.newStringByReplacingKeysInFormat(format, values);
		
		// Logs to file if needed.
		if(shouldLogToFile)
			this.logToFile(sender, logMessage, currentDate);
		
		// Forwards the log message to the registered delegates if needed.
		if(shouldLogToDelegates)
		{
			this.getDelegatesController().notifyObservers(new ObserversController.NotificationBlock<Delegate>()
			{
				@Override public void execute(@NonNull Delegate delegate)
				{
					delegate.logMessage(Logger.this, logMessage, currentDate);
				}
			});
		}
	}
	
	public void log(@NonNull String sender, @NonNull String message, @NonNull Severity severity)
	{
		this.log(sender, message, Output.ALL, severity, Tags.NONE);
	}
	
	public void log(@NonNull String sender, @NonNull String message, @NonNull Severity severity, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, severity, tags);
	}
	
	private void logToConsole(@NonNull String sender, @NonNull String message, @NonNull Severity severity, @NonNull Date currentDate)
	{
		switch(severity)
		{
			case EMERGENCY:
			case ALERT:
			case CRITICAL:
			case ERROR:
			{
				Log.e(sender, message);
				break;
			}
			case WARNING:
			{
				Log.w(sender, message);
				break;
			}
			case NOTICE:
			case INFO:
			{
				Log.i(sender, message);
				break;
			}
			case DEBUG:
			{
				Log.d(sender, message);
				break;
			}
		}
	}
	
	private void logToFile(@NonNull String sender, @NonNull String message, @NonNull Date currentDate)
	{
		Context context = this.getContext();
		if(context == null)
			return;
		
		File file = this.fileForDate(currentDate, context);
		synchronized(this)
		{
			if(!this.createFile(file, currentDate))
			{
				String tagsString = Tags.stringFromTags(EnumSet.of(Tags.ERROR, Tags.FILE_SYSTEM));
				Log.e("", String.format(Locale.US, "Failed to create log file at path '%s'. %s", file.getPath(), tagsString));
				return;
			}
			
			FileOutputStream outputStream;
			try
			{
				outputStream = new FileOutputStream(file, true);
			}
			catch(FileNotFoundException e)
			{
				String tagsString = Tags.stringFromTags(EnumSet.of(Tags.ERROR, Tags.FILE_SYSTEM));
				Log.e("", String.format(Locale.US, "Failed to open output stream for log file at path '%s'. %s", file.getPath(), tagsString), e);
				return;
			}
			
			OutputStreamWriter writer = new OutputStreamWriter(outputStream);
			try
			{
				writer.write(message);
				writer.write("\n");
				writer.flush();
			}
			catch(IOException e)
			{
				String tagsString = Tags.stringFromTags(EnumSet.of(Tags.ERROR, Tags.FILE_SYSTEM));
				Log.e("", String.format(Locale.US, "Failed to write to output stream for log file at path '%s'. %s", file.getPath(), tagsString), e);
			}
			
			try
			{
				writer.close();
				outputStream.close();
			}
			catch(IOException e)
			{
				String tagsString = Tags.stringFromTags(EnumSet.of(Tags.ERROR, Tags.FILE_SYSTEM));
				Log.e("", String.format(Locale.US, "Failed to close output stream for log file at path '%s'. %s", file.getPath(), tagsString), e);
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Service management (Convenience)
	
	public void logAlert(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.ALERT, tags);
	}
	
	public void logCritical(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.CRITICAL, tags);
	}
	
	public void logDebug(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.DEBUG, tags);
	}
	
	public void logEmergency(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.EMERGENCY, tags);
	}
	
	public void logError(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.ERROR, tags);
	}
	
	public void logInfo(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.INFO, tags);
	}
	
	public void logNotice(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.NOTICE, tags);
	}
	
	public void logWarning(@NonNull String sender, @NonNull String message, @NonNull EnumSet<Tags> tags)
	{
		this.log(sender, message, Output.ALL, Severity.WARNING, tags);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
