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

package com.jackfelle.jfkit.layout;

import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.data.Error;
import com.jackfelle.jfkit.data.Hook;
import com.jackfelle.jfkit.data.Strings;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;

public abstract class Dialogs
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	public static final int TIMEOUT_DISABLED;
	public static final int TIMEOUT_LONG;
	public static final int TIMEOUT_SHORT;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	static
	{
		TIMEOUT_DISABLED = 0;
		TIMEOUT_LONG = 7000;
		TIMEOUT_SHORT = 4000;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Layout
	
	public static void presentAlert(@NonNull Context context, @Nullable String title, @Nullable String message, @Nullable Button positiveButton, @Nullable Button neutralButton, @Nullable Button negativeButton, @Nullable Blocks.Block onDismissBlock, int timeout, @Nullable Blocks.Block onTimeoutBlock)
	{
		AlertDialog.Builder builder = Dialogs.newBuilder(context, title, message);
		Dialogs.setButtons(builder, positiveButton, neutralButton, negativeButton, null, android.R.string.ok);
		Dialogs.createAndShowDialog(builder, Dialogs.setOnDismissAndOnTimeoutBlocks(builder, onDismissBlock, timeout, onTimeoutBlock));
	}
	
	public static void presentError(@NonNull Context context, @NonNull Throwable throwable, @Nullable Button positiveButton, @Nullable Button neutralButton, @Nullable Button negativeButton, @Nullable Blocks.Block onDismissBlock, int timeout, @Nullable Blocks.Block onTimeoutBlock)
	{
		String message = null;
		String title = throwable.getLocalizedMessage();
		
		Error error = null;
		if(throwable instanceof Error)
		{
			error = (Error)throwable;
			
			String failureReason = error.getLocalizedFailureReason();
			String recoverySuggestion = error.getLocalizedRecoverySuggestion();
			
			if((failureReason != null) && (recoverySuggestion != null))
				message = failureReason + " " + recoverySuggestion;
			else if(failureReason != null)
				message = failureReason;
			else if(recoverySuggestion != null)
				message = recoverySuggestion;
			
			if(Strings.isEmptyString(message))
				message = null;
		}
		
		if((title == null) && (message == null))
		{
			title = throwable.getClass().getSimpleName();
			if(error != null)
				message = String.format(Locale.getDefault(), "Domain: %s\nCode: %d", error.getDomain(), error.getCode());
		}
		
		Dialogs.presentAlert(context, title, message, positiveButton, neutralButton, negativeButton, onDismissBlock, timeout, onTimeoutBlock);
	}
	
	public static void presentForm(@NonNull Context context, @Nullable String title, @Nullable String message, @NonNull List<FormFieldConfigurator> fieldConfigurators, @Nullable FormButton positiveButton, @Nullable FormButton neutralButton, @Nullable FormButton negativeButton, @Nullable Blocks.Block onDismissBlock, int timeout, @Nullable Blocks.Block onTimeoutBlock)
	{
		AlertDialog.Builder builder = Dialogs.newBuilder(context, title, message);
		EditText[] fields = Dialogs.setFormFields(builder, fieldConfigurators);
		Dialogs.setFormButtons(builder, positiveButton, neutralButton, negativeButton, fields, android.R.string.ok);
		Dialogs.createAndShowDialog(builder, Dialogs.setOnDismissAndOnTimeoutBlocks(builder, onDismissBlock, timeout, onTimeoutBlock));
	}
	
	public static void presentSheet(@NonNull Context context, @Nullable String title, @Nullable Button cancelButton, @Nullable Button destructiveButton, @Nullable List<Button> otherButtons, @Nullable Blocks.Block onDismissBlock, int timeout, @Nullable Blocks.Block onTimeoutBlock)
	{
		AlertDialog.Builder builder = Dialogs.newBuilder(context, title, null);
		Dialogs.setButtons(builder, null, cancelButton, destructiveButton, otherButtons, android.R.string.cancel);
		Dialogs.createAndShowDialog(builder, Dialogs.setOnDismissAndOnTimeoutBlocks(builder, onDismissBlock, timeout, onTimeoutBlock));
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Layout (Convenience)
	
	public static void presentAlert(@NonNull Context context, @StringRes int title, @StringRes int message, @Nullable Button neutralButton)
	{
		Dialogs.presentAlert(context, context.getString(title), context.getString(message), null, neutralButton, null, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentAlert(@NonNull Context context, @Nullable String title, @Nullable String message, @Nullable Button neutralButton)
	{
		Dialogs.presentAlert(context, title, message, null, neutralButton, null, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentAlert(@NonNull Context context, @StringRes int title, @StringRes int message, @Nullable Button positiveButton, @Nullable Button neutralButton, @Nullable Button negativeButton)
	{
		Dialogs.presentAlert(context, context.getString(title), context.getString(message), positiveButton, neutralButton, negativeButton, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentAlert(@NonNull Context context, @Nullable String title, @Nullable String message, @Nullable Button positiveButton, @Nullable Button neutralButton, @Nullable Button negativeButton)
	{
		Dialogs.presentAlert(context, title, message, positiveButton, neutralButton, negativeButton, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentError(@NonNull Context context, @NonNull Throwable throwable, @Nullable Button neutralButton)
	{
		Dialogs.presentError(context, throwable, null, neutralButton, null, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentError(@NonNull Context context, @NonNull Throwable throwable, @Nullable Button positiveButton, @Nullable Button neutralButton, @Nullable Button negativeButton)
	{
		Dialogs.presentError(context, throwable, positiveButton, neutralButton, negativeButton, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentForm(@NonNull Context context, @StringRes int title, @StringRes int message, @NonNull List<FormFieldConfigurator> fieldConfigurators, @Nullable FormButton neutralButton)
	{
		Dialogs.presentForm(context, context.getString(title), context.getString(message), fieldConfigurators, null, neutralButton, null, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentForm(@NonNull Context context, @Nullable String title, @Nullable String message, @NonNull List<FormFieldConfigurator> fieldConfigurators, @Nullable FormButton neutralButton)
	{
		Dialogs.presentForm(context, title, message, fieldConfigurators, null, neutralButton, null, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentForm(@NonNull Context context, @StringRes int title, @StringRes int message, @NonNull List<FormFieldConfigurator> fieldConfigurators, @Nullable FormButton positiveButton, @Nullable FormButton neutralButton, @Nullable FormButton negativeButton)
	{
		Dialogs.presentForm(context, context.getString(title), context.getString(message), fieldConfigurators, positiveButton, neutralButton, negativeButton, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentForm(@NonNull Context context, @Nullable String title, @Nullable String message, @NonNull List<FormFieldConfigurator> fieldConfigurators, @Nullable FormButton positiveButton, @Nullable FormButton neutralButton, @Nullable FormButton negativeButton)
	{
		Dialogs.presentForm(context, title, message, fieldConfigurators, positiveButton, neutralButton, negativeButton, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentSheet(@NonNull Context context, @StringRes int title, @Nullable Button cancelButton, @Nullable Button destructiveButton, @Nullable List<Button> otherButtons)
	{
		Dialogs.presentSheet(context, context.getString(title), cancelButton, destructiveButton, otherButtons, null, TIMEOUT_DISABLED, null);
	}
	
	public static void presentSheet(@NonNull Context context, @Nullable String title, @Nullable Button cancelButton, @Nullable Button destructiveButton, @Nullable List<Button> otherButtons)
	{
		Dialogs.presentSheet(context, title, cancelButton, destructiveButton, otherButtons, null, TIMEOUT_DISABLED, null);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Timers
	
	public static boolean isTimeoutEnabled(int timeout)
	{
		return (timeout > TIMEOUT_DISABLED);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	private static void createAndShowDialog(@NonNull AlertDialog.Builder builder, @Nullable Pair<Hook<AlertDialog>, CountDownTimer> pair)
	{
		Hook<AlertDialog> hook = null;
		CountDownTimer timer = null;
		
		if(pair != null)
		{
			hook = pair.first;
			timer = pair.second;
		}
		
		AlertDialog dialog = builder.create();
		
		if(hook != null)
			hook.set(dialog);
		
		dialog.show();
		
		if(timer != null)
			timer.start();
	}
	
	private static @Nullable Pair<Hook<AlertDialog>, CountDownTimer> createCountDownTimerIfNeeded(int timeout, final @Nullable Blocks.Block onTimeoutBlock)
	{
		if(!Dialogs.isTimeoutEnabled(timeout))
			return null;
		
		final Hook<AlertDialog> hook = new Hook<>();
		CountDownTimer timer = new CountDownTimer(timeout, timeout)
		{
			@Override public void onTick(long millisUntilFinished)
			{
				// Nothing to do.
			}
			
			@Override public void onFinish()
			{
				AlertDialog dialog = hook.get();
				if(dialog == null)
					return;
				
				if(dialog.isShowing())
					dialog.dismiss();
				
				hook.set(null);
				
				if(onTimeoutBlock != null)
					onTimeoutBlock.execute();
			}
		};
		
		return new Pair<>(hook, timer);
	}
	
	private static @NonNull AlertDialog.Builder newBuilder(@NonNull Context context, @Nullable String title, @Nullable String message)
	{
		AlertDialog.Builder retObj = new AlertDialog.Builder(context);
		retObj.setCancelable(false);
		retObj.setMessage(message);
		retObj.setTitle(title);
		return retObj;
	}
	
	private static @Nullable DialogInterface.OnClickListener newButtonAction(@Nullable Button button)
	{
		if(button == null)
			return null;
		
		final Blocks.Block action = button.getAction();
		if(action == null)
			return null;
		
		return new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				action.execute();
			}
		};
	}
	
	private static @Nullable DialogInterface.OnClickListener newFormButtonAction(@Nullable FormButton button, final @NonNull EditText[] fields)
	{
		if(button == null)
			return null;
		
		final Blocks.BlockWithArray<EditText> action = button.getAction();
		if(action == null)
			return null;
		
		return new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				action.execute(fields);
			}
		};
	}
	
	private static @NonNull EditText newFormField(@NonNull Context context, @NonNull FormFieldConfigurator configurator)
	{
		EditText retObj = new EditText(context);
		retObj.setSingleLine();
		configurator.configure(retObj);
		return retObj;
	}
	
	private static void setButtons(@NonNull AlertDialog.Builder builder, @Nullable Button positiveButton, @Nullable Button neutralButton, @Nullable Button negativeButton, @Nullable List<Button> otherButtons, @StringRes int defaultButtonTitle)
	{
		boolean shouldAddDefaultButton = true;
		
		if(Dialogs.setPositiveButton(builder, positiveButton))
			shouldAddDefaultButton = false;
		
		if(Dialogs.setNeutralButton(builder, neutralButton))
			shouldAddDefaultButton = false;
		
		if(Dialogs.setNegativeButton(builder, negativeButton))
			shouldAddDefaultButton = false;
		
		if(Dialogs.setOtherButtons(builder, otherButtons))
			shouldAddDefaultButton = false;
		
		if(shouldAddDefaultButton)
			Dialogs.setDefaultButton(builder, defaultButtonTitle);
	}
	
	private static void setDefaultButton(@NonNull AlertDialog.Builder builder, @StringRes int buttonTitle)
	{
		builder.setNeutralButton(buttonTitle, null);
	}
	
	private static void setFormButtons(@NonNull AlertDialog.Builder builder, @Nullable FormButton positiveButton, @Nullable FormButton neutralButton, @Nullable FormButton negativeButton, @NonNull EditText[] fields, @StringRes int defaultButtonTitle)
	{
		boolean shouldAddDefaultButton = true;
		
		if(Dialogs.setFormPositiveButton(builder, positiveButton, fields))
			shouldAddDefaultButton = false;
		
		if(Dialogs.setFormNeutralButton(builder, neutralButton, fields))
			shouldAddDefaultButton = false;
		
		if(Dialogs.setFormNegativeButton(builder, negativeButton, fields))
			shouldAddDefaultButton = false;
		
		if(shouldAddDefaultButton)
			Dialogs.setDefaultButton(builder, defaultButtonTitle);
	}
	
	private static @NonNull EditText[] setFormFields(@NonNull AlertDialog.Builder builder, @NonNull List<FormFieldConfigurator> fieldConfigurators)
	{
		int size = fieldConfigurators.size();
		EditText[] retObj = new EditText[size];
		if(size > 0)
		{
			Context context = builder.getContext();
			
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			for(int i = 0; i < size; i++)
			{
				EditText field = Dialogs.newFormField(context, fieldConfigurators.get(i));
				layout.addView(field);
				retObj[i] = field;
			}
			
			builder.setView(layout);
		}
		return retObj;
	}
	
	private static boolean setFormNegativeButton(@NonNull AlertDialog.Builder builder, @Nullable FormButton button, @NonNull EditText[] fields)
	{
		if(button == null)
			return false;
		
		builder.setNegativeButton(button.getTitle(), Dialogs.newFormButtonAction(button, fields));
		return true;
	}
	
	private static boolean setFormNeutralButton(@NonNull AlertDialog.Builder builder, @Nullable FormButton button, @NonNull EditText[] fields)
	{
		if(button == null)
			return false;
		
		builder.setNeutralButton(button.getTitle(), Dialogs.newFormButtonAction(button, fields));
		return true;
	}
	
	private static boolean setFormPositiveButton(@NonNull AlertDialog.Builder builder, @Nullable FormButton button, @NonNull EditText[] fields)
	{
		if(button == null)
			return false;
		
		builder.setPositiveButton(button.getTitle(), Dialogs.newFormButtonAction(button, fields));
		return true;
	}
	
	private static boolean setNegativeButton(@NonNull AlertDialog.Builder builder, @Nullable Button button)
	{
		if(button == null)
			return false;
		
		builder.setNegativeButton(button.getTitle(), Dialogs.newButtonAction(button));
		return true;
	}
	
	private static boolean setNeutralButton(@NonNull AlertDialog.Builder builder, @Nullable Button button)
	{
		if(button == null)
			return false;
		
		builder.setNeutralButton(button.getTitle(), Dialogs.newButtonAction(button));
		return true;
	}
	
	private static @Nullable Pair<Hook<AlertDialog>, CountDownTimer> setOnDismissAndOnTimeoutBlocks(@NonNull AlertDialog.Builder builder, final @Nullable Blocks.Block onDismissBlock, int timeout, @Nullable Blocks.Block onTimeoutBlock)
	{
		Pair<Hook<AlertDialog>, CountDownTimer> retObj = Dialogs.createCountDownTimerIfNeeded(timeout, onTimeoutBlock);
		
		final CountDownTimer timer = ((retObj == null) ? null : retObj.second);
		builder.setOnDismissListener(((onDismissBlock == null) && (timer == null)) ? null : new DialogInterface.OnDismissListener()
		{
			@Override public void onDismiss(DialogInterface dialog)
			{
				if(timer != null)
					timer.cancel();
				if(onDismissBlock != null)
					onDismissBlock.execute();
			}
		});
		
		return retObj;
	}
	
	private static boolean setOtherButtons(@NonNull AlertDialog.Builder builder, @Nullable List<Button> buttons)
	{
		if(buttons == null)
			return false;
		
		int size = buttons.size();
		if(size == 0)
			return false;
		
		final Blocks.Block[] actions = new Blocks.Block[size];
		CharSequence[] titles = new CharSequence[size];
		
		for(int i = 0; i < size; i++)
		{
			Button button = buttons.get(i);
			actions[i] = button.getAction();
			titles[i] = button.getTitle();
		}
		
		builder.setItems(titles, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				Blocks.Block action = actions[which];
				if(action != null)
					action.execute();
			}
		});
		
		return true;
	}
	
	private static boolean setPositiveButton(@NonNull AlertDialog.Builder builder, @Nullable Button button)
	{
		if(button == null)
			return false;
		
		builder.setPositiveButton(button.getTitle(), Dialogs.newButtonAction(button));
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	public static class Button
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Fields
		
		private @Nullable Blocks.Block _action;
		private @NonNull String _title;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Data
		
		public @Nullable Blocks.Block getAction()
		{
			return _action;
		}
		
		public @NonNull String getTitle()
		{
			return _title;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public Button(@NonNull Context context, @StringRes int title)
		{
			this(context.getString(title), null);
		}
		
		public Button(@NonNull Context context, @StringRes int title, @Nullable Blocks.Block action)
		{
			this(context.getString(title), action);
		}
		
		public Button(@NonNull String title)
		{
			this(title, null);
		}
		
		public Button(@NonNull String title, @Nullable Blocks.Block action)
		{
			_action = action;
			_title = title;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public static class FormButton
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Fields
		
		private @Nullable Blocks.BlockWithArray<EditText> _action;
		private @NonNull String _title;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Data
		
		public @Nullable Blocks.BlockWithArray<EditText> getAction()
		{
			return _action;
		}
		
		public @NonNull String getTitle()
		{
			return _title;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public FormButton(@NonNull Context context, @StringRes int title)
		{
			this(context.getString(title), null);
		}
		
		public FormButton(@NonNull Context context, @StringRes int title, @Nullable Blocks.BlockWithArray<EditText> action)
		{
			this(context.getString(title), action);
		}
		
		public FormButton(@NonNull String title)
		{
			this(title, null);
		}
		
		public FormButton(@NonNull String title, @Nullable Blocks.BlockWithArray<EditText> action)
		{
			_action = action;
			_title = title;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface FormFieldConfigurator
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Layout
		
		void configure(@NonNull EditText field);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
