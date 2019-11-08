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

package com.jackfelle.jfkit.utilities;

import android.os.AsyncTask;

import com.jackfelle.jfkit.core.operations.BlockOperation;
import com.jackfelle.jfkit.core.operations.OperationQueue;
import com.jackfelle.jfkit.data.Blocks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.NonNull;

public class ObserversController <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface NotificationBlock <T>
	{
		void execute(@NonNull T observer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private boolean _needsCleanUp = false;
	private @NonNull ArrayList<WeakReference<T>> _references = new ArrayList<>();
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Observers
	
	protected boolean getNeedsCleanUp()
	{
		return _needsCleanUp;
	}
	
	protected void setNeedsCleanUp(boolean needsCleanUp)
	{
		if(needsCleanUp)
		{
			if(_needsCleanUp)
				return;
			
			_needsCleanUp = true;
			
			AsyncTask.execute(new Runnable()
			{
				@Override public void run()
				{
					ObserversController.this.cleanUpIfNeeded();
				}
			});
		}
		else
			_needsCleanUp = false;
	}
	
	private @NonNull ArrayList<WeakReference<T>> getReferences()
	{
		return _references;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Notifications management
	
	public void notifyObservers(@NonNull NotificationBlock<T> notificationBlock)
	{
		this.notifyObservers(OperationQueue.getMainQueue(), notificationBlock, false);
	}
	
	public void notifyObserversNow(@NonNull NotificationBlock<T> notificationBlock)
	{
		ArrayList<WeakReference<T>> references = this.getReferences();
		synchronized(references)
		{
			references = new ArrayList<>(references);
		}
		
		for(WeakReference<T> reference : references)
		{
			T observer = reference.get();
			if(observer != null)
				notificationBlock.execute(observer);
			else
				ObserversController.this.setNeedsCleanUp(true);
		}
	}
	
	public void notifyObservers(@NonNull OperationQueue queue, @NonNull final NotificationBlock<T> notificationBlock, boolean waitUntilFinished)
	{
		ArrayList<WeakReference<T>> references = this.getReferences();
		synchronized(references)
		{
			references = new ArrayList<>(references);
		}
		
		ArrayList<Blocks.Block> operations = new ArrayList<>(references.size());
		for(final WeakReference<T> reference : references)
		{
			Blocks.Block operation = new Blocks.Block()
			{
				@Override public void execute()
				{
					T observer = reference.get();
					if(observer != null)
						notificationBlock.execute(observer);
					else
						ObserversController.this.setNeedsCleanUp(true);
				}
			};
			
			operations.add(operation);
		}
		
		queue.addOperation(new BlockOperation(operations), waitUntilFinished);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers management
	
	public void addObserver(@NonNull T observer)
	{
		ArrayList<WeakReference<T>> references = this.getReferences();
		synchronized(references)
		{
			WeakReference<T> oldReference = this.getReferenceForObserver(observer);
			if(oldReference == null)
				references.add(new WeakReference<T>(observer));
		}
	}
	
	private void cleanUp()
	{
		ArrayList<WeakReference<T>> references = this.getReferences();
		synchronized(references)
		{
			ArrayList<WeakReference<T>> obsolete = new ArrayList<>(references.size());
			for(WeakReference<T> reference : references)
			{
				if(reference.get() == null)
					obsolete.add(reference);
			}
			references.removeAll(obsolete);
		}
	}
	
	private void cleanUpIfNeeded()
	{
		if(this.getNeedsCleanUp())
		{
			this.cleanUp();
			this.setNeedsCleanUp(false);
		}
	}
	
	protected WeakReference<T> getReferenceForObserver(@NonNull T observer)
	{
		ArrayList<WeakReference<T>> references = this.getReferences();
		synchronized(references)
		{
			boolean needsCleanUp = false;
			WeakReference<T> retObj = null;
			
			for(WeakReference<T> reference : references)
			{
				T temp = reference.get();
				if(temp == null)
					needsCleanUp = true;
				
				if(temp == observer)
				{
					retObj = reference;
					break;
				}
			}
			
			if(needsCleanUp)
				this.setNeedsCleanUp(true);
			
			return retObj;
		}
	}
	
	public void removeObserver(@NonNull T observer)
	{
		ArrayList<WeakReference<T>> references = this.getReferences();
		synchronized(references)
		{
			WeakReference<T> oldReference = this.getReferenceForObserver(observer);
			if(oldReference != null)
				references.remove(oldReference);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
