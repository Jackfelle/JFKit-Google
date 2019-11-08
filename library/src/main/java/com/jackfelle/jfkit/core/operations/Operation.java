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

package com.jackfelle.jfkit.core.operations;

import android.util.Log;

import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.utilities.ObserversController;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

public abstract class Operation
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Types (Enumerations)
	
	public enum QueuePriority
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		VERY_LOW,
		LOW,
		NORMAL,
		HIGH,
		VERY_HIGH;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Constants
		
		public static final @NonNull QueuePriority[] SORTED_VALUES_ASC = new Operation.QueuePriority[] {VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH};
		public static final @NonNull QueuePriority[] SORTED_VALUES_DESC = new Operation.QueuePriority[] {VERY_HIGH, HIGH, NORMAL, LOW, VERY_LOW};
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Types (Interfaces)
	
	public interface Observer
	{
		// Methods - Notifications management
		void operationIsCancelled(@NonNull Operation sender);
		void operationIsExecuting(@NonNull Operation sender);
		void operationIsFinished(@NonNull Operation sender);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private String _name;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Execution
	
	private Blocks.Block _completion;
	private Set<Operation> _dependencies;
	private QueuePriority _queuePriority;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private @NonNull ObserversController<Observer> _observersController;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - State
	
	private boolean _cancelled;
	private boolean _executing;
	private boolean _finished;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Data
	
	public String getName()
	{
		synchronized(this)
		{
			return _name;
		}
	}
	
	public void setName(String name)
	{
		synchronized(this)
		{
			_name = name;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Execution
	
	public Blocks.Block getCompletion()
	{
		synchronized(this)
		{
			return _completion;
		}
	}
	
	public void setCompletion(Blocks.Block completion)
	{
		synchronized(this)
		{
			_completion = completion;
		}
	}
	
	public @NonNull Set<Operation> getDependencies()
	{
		Set<Operation> retObj = this.getDependencies(false);
		return ((retObj == null) ? new HashSet<Operation>() : new HashSet<>(retObj));
	}
	
	protected Set<Operation> getDependencies(boolean createIfNeeded)
	{
		if((_dependencies == null) && createIfNeeded)
		{
			synchronized(this)
			{
				if(_dependencies == null)
					_dependencies = new HashSet<>();
			}
		}
		return _dependencies;
	}
	
	public @NonNull QueuePriority getQueuePriority()
	{
		synchronized(this)
		{
			return _queuePriority;
		}
	}
	
	public void setQueuePriority(@NonNull QueuePriority queuePriority)
	{
		synchronized(this)
		{
			_queuePriority = queuePriority;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Observers
	
	protected @NonNull ObserversController<Observer> getObserversController()
	{
		return _observersController;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - State
	
	public boolean isAsynchronous()
	{
		return false;
	}
	
	public boolean isCancelled()
	{
		synchronized(this)
		{
			return _cancelled;
		}
	}
	
	public boolean isExecuting()
	{
		synchronized(this)
		{
			return _executing;
		}
	}
	
	public boolean isFinished()
	{
		synchronized(this)
		{
			return _finished;
		}
	}
	
	public boolean isReady()
	{
		synchronized(this)
		{
			if(_executing || _finished)
				return false;
		}
		
		Set<Operation> dependencies = this.getDependencies(false);
		if(dependencies != null)
		{
			synchronized(dependencies)
			{
				for(Operation operation : dependencies)
				{
					if(!operation.isFinished())
						return false;
				}
			}
		}
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory management
	
	public Operation()
	{
		// Data
		_name = null;
		
		// Execution
		_completion = null;
		_dependencies = null;
		_queuePriority = QueuePriority.NORMAL;
		
		// Observers
		_observersController = new ObserversController<>();
		
		// State
		_cancelled = false;
		_executing = false;
		_finished = false;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Execution management
	
	public <T extends Operation> void addDependencies(@NonNull Collection<T> operations)
	{
		if(operations.size() == 0)
			return;
		
		Set<Operation> dependencies = this.getDependencies(true);
		synchronized(dependencies)
		{
			dependencies.addAll(operations);
		}
	}
	
	public <T extends Operation> void addDependency(@NonNull T dependency)
	{
		Set<Operation> dependencies = this.getDependencies(true);
		synchronized(dependencies)
		{
			dependencies.add(dependency);
		}
	}
	
	public void cancel()
	{
		synchronized(this)
		{
			if(_cancelled)
				return;
			
			_cancelled = true;
		}
		
		this.getObserversController().notifyObserversNow(new ObserversController.NotificationBlock<Observer>()
		{
			@Override public void execute(@NonNull Observer observer)
			{
				observer.operationIsCancelled(Operation.this);
			}
		});
	}
	
	protected void finish()
	{
		synchronized(this)
		{
			if(_finished || (!_executing && !_cancelled))
				return;
			
			_executing = false;
			_finished = true;
			
			this.notifyAll();
		}
		
		Blocks.Block completion = this.getCompletion();
		if(completion != null)
			completion.execute();
		
		this.getObserversController().notifyObserversNow(new ObserversController.NotificationBlock<Observer>()
		{
			@Override public void execute(@NonNull Observer observer)
			{
				observer.operationIsFinished(Operation.this);
			}
		});
	}
	
	protected void main()
	{
	}
	
	public <T extends Operation> void removeDependencies(@NonNull Collection<T> operations)
	{
		if(operations.size() == 0)
			return;
		
		Set<Operation> dependencies = this.getDependencies(false);
		if(dependencies == null)
			return;
		
		synchronized(dependencies)
		{
			dependencies.removeAll(operations);
		}
	}
	
	public <T extends Operation> void removeDependency(@NonNull T operation)
	{
		Set<Operation> dependencies = this.getDependencies(false);
		if(dependencies == null)
			return;
		
		synchronized(dependencies)
		{
			dependencies.remove(operation);
		}
	}
	
	public void start()
	{
		if(this.isCancelled())
		{
			this.finish();
			return;
		}
		
		synchronized(this)
		{
			if(!this.isReady())
				return;
			
			_executing = true;
		}
		
		this.getObserversController().notifyObserversNow(new ObserversController.NotificationBlock<Observer>()
		{
			@Override public void execute(@NonNull Observer observer)
			{
				observer.operationIsExecuting(Operation.this);
			}
		});
		
		this.main();
		
		if(!this.isAsynchronous())
			this.finish();
	}
	
	public void waitUntilFinished()
	{
		synchronized(this)
		{
			try
			{
				while(!_finished)
					this.wait();
			}
			catch(InterruptedException e)
			{
				Log.e("JFFramework", "Thread interrupted.", e);
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers management
	
	public void addObserver(@NonNull Observer observer)
	{
		this.getObserversController().addObserver(observer);
	}
	
	public void removeObserver(@NonNull Observer observer)
	{
		this.getObserversController().removeObserver(observer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
