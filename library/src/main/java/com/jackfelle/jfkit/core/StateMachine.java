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

package com.jackfelle.jfkit.core;

import com.jackfelle.jfkit.core.operations.OperationQueue;
import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.data.Error;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class StateMachine
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	// Error codes
	public static final int ERROR_INVALID_FINAL_STATE_ON_FAILURE = 1;
	public static final int ERROR_INVALID_FINAL_STATE_ON_SUCCESS = 2;
	public static final int ERROR_INVALID_INITIAL_STATE = 3;
	public static final int ERROR_INVALID_TRANSITION = 4;
	public static final int ERROR_WRONG_INITIAL_STATE = 5;
	
	// States
	public static final int STATE_NOT_AVAILABLE = Integer.MAX_VALUE;
	
	// Transitions
	public static final int TRANSITION_NONE = 0;
	public static final int TRANSITION_NOT_AVAILABLE = Integer.MAX_VALUE;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Delegate
	{
		// State management
		void stateMachineDidPerformTransition(@NonNull StateMachine sender, int transition, Object context);
		void stateMachinePerformTransition(@NonNull StateMachine sender, int transition, Object context, @NonNull Blocks.SimpleCompletionBlock completion);
		void stateMachineWillPerformTransition(@NonNull StateMachine sender, int transition, Object context);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	// Concurrency
	private @NonNull OperationQueue _notificationQueue;
	private @NonNull OperationQueue _transitionQueue;
	
	// Observers
	private @NonNull WeakReference<Delegate> _delegate;
	
	// State
	private int _currentState;
	private int _currentTransition;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors
	
	public int getCurrentState()
	{
		synchronized(this)
		{
			return _currentState;
		}
	}
	
	public int getCurrentTransition()
	{
		synchronized(this)
		{
			return _currentTransition;
		}
	}
	
	private void setCurrentStateAndTransition(int state, int transition)
	{
		synchronized(this)
		{
			_currentState = state;
			_currentTransition = transition;
		}
	}
	
	public Delegate getDelegate()
	{
		return _delegate.get();
	}
	
	private @NonNull OperationQueue getNotificationQueue()
	{
		return _notificationQueue;
	}
	
	private @NonNull OperationQueue getTransitionQueue()
	{
		return _transitionQueue;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Memory management
	
	public StateMachine(int state, @NonNull Delegate delegate)
	{
		super();
		
		String queueName = this.getClass().getSimpleName();
		
		_currentState = state;
		_currentTransition = TRANSITION_NONE;
		_delegate = new WeakReference<>(delegate);
		_notificationQueue = OperationQueue.newSerialQueue(queueName + ".notifications");
		_transitionQueue = OperationQueue.newSerialQueue(queueName + ".transitions");
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Concurrency management
	
	private void executeOnNotificationQueueAndWaitUntilFinished(@NonNull Blocks.Block block)
	{
		this.getNotificationQueue().addOperation(block, true);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region State management
	
	private void completeTransition(final boolean succeeded, final Throwable error, final Object context, final Blocks.SimpleCompletionBlock completion)
	{
		final int transition = this.getCurrentTransition();
		
		int finalState = (succeeded ? this.getFinalStateForSucceededTransition(transition) : this.getFinalStateForFailedTransition(transition));
		
		this.setCurrentStateAndTransition(finalState, TRANSITION_NONE);
		
		final Delegate delegate = this.getDelegate();
		if(delegate != null)
		{
			this.executeOnNotificationQueueAndWaitUntilFinished(new Blocks.Block()
			{
				@Override public void execute()
				{
					delegate.stateMachineDidPerformTransition(StateMachine.this, transition, context);
				}
			});
		}
		
		if(completion != null)
		{
			this.executeOnNotificationQueueAndWaitUntilFinished(new Blocks.Block()
			{
				@Override public void execute()
				{
					completion.execute(succeeded, error);
				}
			});
		}
		
		this.getTransitionQueue().setSuspended(false);
	}
	
	public int getFinalStateForFailedTransition(int transition)
	{
		return STATE_NOT_AVAILABLE;
	}
	
	public int getFinalStateForSucceededTransition(int transition)
	{
		return STATE_NOT_AVAILABLE;
	}
	
	public int getInitialStateForTransition(int transition)
	{
		return STATE_NOT_AVAILABLE;
	}
	
	private Error isValidTransition(int transition)
	{
		if((transition == TRANSITION_NONE) || (transition == TRANSITION_NOT_AVAILABLE))
			return new Error("", ERROR_INVALID_TRANSITION, null);
		
		int state = this.getInitialStateForTransition(transition);
		if(state == STATE_NOT_AVAILABLE)
			return new Error("", ERROR_INVALID_INITIAL_STATE);
		
		state = this.getFinalStateForSucceededTransition(transition);
		if(state == STATE_NOT_AVAILABLE)
			return new Error("", ERROR_INVALID_FINAL_STATE_ON_SUCCESS);
		
		state = this.getFinalStateForFailedTransition(transition);
		if(state == STATE_NOT_AVAILABLE)
			return new Error("", ERROR_INVALID_FINAL_STATE_ON_FAILURE);
		
		return null;
	}
	
	public void performTransition(int transition, Blocks.SimpleCompletionBlock completion)
	{
		this.performTransition(transition, null, completion);
	}
	
	public void performTransition(final int transition, final Object context, final Blocks.SimpleCompletionBlock completion)
	{
		final Blocks.BlockWithError errorBlock = new Blocks.BlockWithError()
		{
			@Override public void execute(final Throwable error)
			{
				if(completion != null)
				{
					StateMachine.this.executeOnNotificationQueueAndWaitUntilFinished(new Blocks.Block()
					{
						@Override public void execute()
						{
							completion.execute(false, error);
						}
					});
				}
			}
		};
		
		Error error = this.isValidTransition(transition);
		if(error != null)
		{
			errorBlock.execute(error);
			return;
		}
		
		Blocks.Block block = new Blocks.Block()
		{
			@Override public void execute()
			{
				if(StateMachine.this.getInitialStateForTransition(transition) != StateMachine.this.getCurrentState())
				{
					errorBlock.execute(new Error("", ERROR_WRONG_INITIAL_STATE));
					return;
				}
				
				StateMachine.this.performTransitionOnQueue(transition, context, completion);
			}
		};
		
		this.getTransitionQueue().addOperation(block);
	}
	
	private void performTransitionOnQueue(final int transition, final Object context, final Blocks.SimpleCompletionBlock completion)
	{
		this.getTransitionQueue().setSuspended(false);
		
		final Delegate delegate = this.getDelegate();
		if(delegate != null)
		{
			this.executeOnNotificationQueueAndWaitUntilFinished(new Blocks.Block()
			{
				@Override public void execute()
				{
					delegate.stateMachineWillPerformTransition(StateMachine.this, transition, context);
				}
			});
		}
		
		this.setCurrentStateAndTransition(this.getCurrentState(), transition);
		
		final Blocks.SimpleCompletionBlock transitionCompletion = new Blocks.SimpleCompletionBlock()
		{
			@Override public void execute(boolean succeeded, Throwable error)
			{
				StateMachine.this.completeTransition(succeeded, error, context, completion);
			}
		};
		
		if(delegate != null)
		{
			this.executeOnNotificationQueueAndWaitUntilFinished(new Blocks.Block()
			{
				@Override public void execute()
				{
					delegate.stateMachinePerformTransition(StateMachine.this, transition, context, transitionCompletion);
				}
			});
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Utilities management
	
	public String getDebugStringForState(int state)
	{
		return ((state == STATE_NOT_AVAILABLE) ? "NotAvailable" : null);
	}
	
	public String getDebugStringForTransition(int transition)
	{
		switch(transition)
		{
			case TRANSITION_NONE:
				return "None";
			case TRANSITION_NOT_AVAILABLE:
				return "NotAvailable";
			default:
				return null;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
