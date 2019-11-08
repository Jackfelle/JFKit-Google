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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.utilities.ObjectIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OperationQueue implements Operation.Observer
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	private static final String TAG = OperationQueue.class.getSimpleName();
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Memory
	
	private static OperationQueue _backgroundOperationQueue;
	private static OperationQueue _mainOperationQueue;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Concurrency
	
	private int _executingConcurrentOperationCount;
	private @NonNull Handler _mainHandler;
	private int _maxConcurrentOperationCount;
	private boolean _needsStartWorkers;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private String _name;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Execution
	
	private boolean _mainQueue;
	private @NonNull Map<Operation.QueuePriority, List<Operation>> _queues;
	private boolean _suspended;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Concurrency
	
	private static int getRuntimeAvailableProcessors()
	{
		return Runtime.getRuntime().availableProcessors();
	}
	
	private int getExecutingConcurrentOperationCount()
	{
		synchronized(this)
		{
			return _executingConcurrentOperationCount;
		}
	}
	
	protected @NonNull Handler getMainHandler()
	{
		return _mainHandler;
	}
	
	public int getMaxConcurrentOperationCount()
	{
		synchronized(this)
		{
			return _maxConcurrentOperationCount;
		}
	}
	
	public void setMaxConcurrentOperationCount(int maxConcurrentOperationCount)
	{
		if(this.isMainQueue())
			maxConcurrentOperationCount = 1;
		else if(maxConcurrentOperationCount < 1)
			maxConcurrentOperationCount = OperationQueue.getRuntimeAvailableProcessors();
		
		synchronized(this)
		{
			if(_maxConcurrentOperationCount == maxConcurrentOperationCount)
				return;
			
			int oldVal = _maxConcurrentOperationCount;
			_maxConcurrentOperationCount = maxConcurrentOperationCount;
			
			Log.i(TAG, String.format(Locale.US, "OperationQueue<%d> did change max concurrent operation count from '%d' to '%d'.", ObjectIdentifier.getID(this), oldVal, maxConcurrentOperationCount));
			
			if(oldVal >= maxConcurrentOperationCount)
				return;
		}
		
		this.setNeedsStartWorkers();
	}
	
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
	
	private boolean isExecuting()
	{
		return (this.getExecutingConcurrentOperationCount() > 0);
	}
	
	public boolean isMainQueue()
	{
		return _mainQueue;
	}
	
	public @NonNull List<Operation> getOperations()
	{
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		
		List<Operation> retObj = new ArrayList<>();
		for(List<Operation> queue : queues.values())
		{
			synchronized(queue)
			{
				retObj.addAll(queue);
			}
		}
		return retObj;
	}
	
	private @NonNull Map<Operation.QueuePriority, List<Operation>> getQueues()
	{
		return _queues;
	}
	
	protected void setNeedsStartWorkers()
	{
		synchronized(this)
		{
			if(_needsStartWorkers)
				return;
			
			_needsStartWorkers = true;
		}
		
		new Thread(new Runnable()
		{
			@Override public void run()
			{
				synchronized(this)
				{
					if(!_needsStartWorkers)
						return;
					
					_needsStartWorkers = false;
				}
				
				OperationQueue.this.startWorkers();
			}
		}).start();
	}
	
	public boolean isSuspended()
	{
		synchronized(this)
		{
			return _suspended;
		}
	}
	
	public void setSuspended(boolean suspended)
	{
		synchronized(this)
		{
			if(_suspended == suspended)
				return;
			
			_suspended = suspended;
			
			if(!_suspended)
				this.setNeedsStartWorkers();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Memory management
	
	public static @NonNull OperationQueue getBackgroundQueue()
	{
		if(_backgroundOperationQueue == null)
		{
			synchronized(OperationQueue.class)
			{
				if(_backgroundOperationQueue == null)
					_backgroundOperationQueue = OperationQueue.newQueue(OperationQueue.class.getSimpleName() + ".background", Integer.MAX_VALUE);
			}
		}
		return _backgroundOperationQueue;
	}
	
	public static @NonNull OperationQueue getMainQueue()
	{
		if(_mainOperationQueue == null)
		{
			synchronized(OperationQueue.class)
			{
				if(_mainOperationQueue == null)
				{
					OperationQueue queue = new OperationQueue(true);
					queue.setName(OperationQueue.class.getSimpleName() + ".main");
					_mainOperationQueue = queue;
				}
			}
		}
		return _mainOperationQueue;
	}
	
	public static @NonNull OperationQueue newConcurrentQueue(@Nullable String name)
	{
		return OperationQueue.newQueue(name, 0);
	}
	
	public static @NonNull OperationQueue newSerialQueue(@Nullable String name)
	{
		return OperationQueue.newQueue(name, 1);
	}
	
	public static @NonNull OperationQueue newQueue(@Nullable String name, int maxConcurrentOperations)
	{
		OperationQueue retObj = new OperationQueue();
		retObj.setMaxConcurrentOperationCount(maxConcurrentOperations);
		retObj.setName(name);
		return retObj;
	}
	
	public OperationQueue()
	{
		this(false);
	}
	
	protected OperationQueue(boolean mainQueue)
	{
		Operation.QueuePriority[] queuePriorities = Operation.QueuePriority.values();
		Map<Operation.QueuePriority, List<Operation>> queues = new HashMap<>(queuePriorities.length);
		for(Operation.QueuePriority queuePriority : queuePriorities)
			queues.put(queuePriority, new LinkedList<Operation>());
		
		// Concurrency
		_executingConcurrentOperationCount = 0;
		_mainHandler = new Handler(Looper.getMainLooper());
		_maxConcurrentOperationCount = (mainQueue ? 1 : OperationQueue.getRuntimeAvailableProcessors());
		_needsStartWorkers = false;
		
		// Execution
		_mainQueue = mainQueue;
		_queues = queues;
		_suspended = false;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Concurrency management
	
	private void startWorkers()
	{
		int currentWorkers;
		int maxWorkers;
		
		synchronized(this)
		{
			if(_suspended)
				return;
			
			currentWorkers = _executingConcurrentOperationCount;
			maxWorkers = _maxConcurrentOperationCount;
		}
		
		if(!this.isMainQueue())
		{
			int availableJobs = 0;
			for(Operation operation : this.getOperations())
			{
				if(operation.isReady() && (++availableJobs >= maxWorkers))
					break;
			}
			maxWorkers = availableJobs;
		}
		
		if(currentWorkers >= maxWorkers)
			return;
		
		Runnable runnable = new Runnable()
		{
			@Override public void run()
			{
				OperationQueue thisQueue = OperationQueue.this;
				
				while(thisQueue.executeNextOperation())
				{
					synchronized(thisQueue)
					{
						if(_suspended || (_executingConcurrentOperationCount > _maxConcurrentOperationCount))
							break;
					}
				}
				
				synchronized(thisQueue)
				{
					_executingConcurrentOperationCount--;
				}
			}
		};
		
		synchronized(this)
		{
			for(int i = currentWorkers; i < maxWorkers; i++)
			{
				_executingConcurrentOperationCount++;
				new Thread(runnable).start();
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Execution management
	
	private boolean executeNextOperation()
	{
		Operation operation = null;
		
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(Operation.QueuePriority queuePriority : Operation.QueuePriority.SORTED_VALUES_DESC)
		{
			List<Operation> queue = queues.get(queuePriority);
			if(queue == null)
				continue;
			
			synchronized(queue)
			{
				for(int i = 0; i < queue.size(); i++)
				{
					Operation temp = queue.get(i);
					if(temp.isReady())
					{
						operation = temp;
						break;
					}
				}
			}
			
			if(operation != null)
				break;
		}
		
		if(operation == null)
			return false;
		
		if(this.isMainQueue())
		{
			final Operation finalOperation = operation;
			this.getMainHandler().post(new Runnable()
			{
				@Override public void run()
				{
					finalOperation.start();
				}
			});
		}
		else
			operation.start();
		
		operation.waitUntilFinished();
		
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Operations management
	
	public void addOperation(@NonNull Blocks.Block executionBlock)
	{
		this.addOperation(new BlockOperation(executionBlock), false);
	}
	
	public void addOperation(@NonNull Blocks.Block executionBlock, boolean waitUntilFinished)
	{
		this.addOperation(new BlockOperation(executionBlock), waitUntilFinished);
	}
	
	public void addOperation(@NonNull Operation operation)
	{
		this.addOperation(operation, false);
	}
	
	public void addOperation(@NonNull Operation operation, boolean waitUntilFinished)
	{
		List<Operation> operations = new ArrayList<>();
		operations.add(operation);
		this.addOperations(operations, waitUntilFinished);
	}
	
	public void addOperations(@NonNull List<Operation> operations)
	{
		this.addOperations(operations, false);
	}
	
	public void addOperations(@NonNull List<Operation> operations, boolean waitUntilFinished)
	{
		if(operations.size() == 0)
			return;
		
		operations = new ArrayList<>(operations);
		
		List<Operation> invalidOperations = null;
		for(Operation operation : operations)
		{
			if(operation.isExecuting() || operation.isFinished())
			{
				if(invalidOperations == null)
					invalidOperations = new ArrayList<>(operations.size());
				invalidOperations.add(operation);
			}
		}
		if(invalidOperations != null)
		{
			operations.removeAll(invalidOperations);
			if(operations.size() == 0)
				return;
		}
		
		Map<Operation.QueuePriority, List<Operation>> operationsByPriority = new HashMap<>();
		for(Operation operation : operations)
		{
			Operation.QueuePriority queuePriority = operation.getQueuePriority();
			List<Operation> queue = operationsByPriority.get(operation.getQueuePriority());
			if(queue == null)
			{
				queue = new ArrayList<>(1);
				operationsByPriority.put(queuePriority, queue);
			}
			queue.add(operation);
		}
		
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(Operation.QueuePriority queuePriority : operationsByPriority.keySet())
		{
			List<Operation> newOperations = operationsByPriority.get(queuePriority);
			List<Operation> queue = queues.get(queuePriority);
			synchronized(queue)
			{
				queue.addAll(newOperations);
			}
		}
		
		for(Operation operation : operations)
			operation.addObserver(this);
		
		this.setNeedsStartWorkers();
		
		if(waitUntilFinished)
		{
			for(Operation operation : operations)
				operation.waitUntilFinished();
		}
	}
	
	public void cancelAllOperations()
	{
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(List<Operation> queue : queues.values())
		{
			synchronized(queue)
			{
				for(Operation operation : queue)
				{
					if(!operation.isFinished())
						operation.cancel();
				}
			}
		}
	}
	
	public void waitUntilAllOperationsAreFinished()
	{
		List<Operation> operations = this.getOperations();
		for(Operation operation : operations)
			operation.waitUntilFinished();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces (Operation.Observer)
	
	@Override public void operationIsCancelled(@NonNull Operation sender)
	{
		// Nothing to do.
	}
	
	@Override public void operationIsExecuting(@NonNull Operation sender)
	{
		// Nothing to do.
	}
	
	@Override public void operationIsFinished(@NonNull Operation sender)
	{
		sender.removeObserver(this);
		
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(List<Operation> queue : queues.values())
		{
			synchronized(queue)
			{
				queue.remove(sender);
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
