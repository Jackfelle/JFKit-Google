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

package com.jackfelle.jfkit.data;

import com.jackfelle.jfkit.core.operations.OperationQueue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Blocks
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public static <T> void executeCompletion(@Nullable Blocks.Completion<T> completion, @NonNull Throwable error)
	{
		if(completion != null)
			completion.executeWithError(error);
	}
	
	public static <T> void executeCompletion(@Nullable Blocks.Completion<T> completion, @NonNull Throwable error, boolean async)
	{
		if(completion != null)
			completion.executeWithError(error, async);
	}
	
	public static <T> void executeCompletion(@Nullable Blocks.Completion<T> completion, @NonNull T result)
	{
		if(completion != null)
			completion.executeWithResult(result);
	}
	
	public static <T> void executeCompletion(@Nullable Blocks.Completion<T> completion, @NonNull T result, boolean async)
	{
		if(completion != null)
			completion.executeWithResult(result, async);
	}
	
	public static void executeCompletion(@Nullable Blocks.SimpleCompletion completion, @NonNull Throwable error)
	{
		if(completion != null)
			completion.executeWithError(error);
	}
	
	public static void executeCompletion(@Nullable Blocks.SimpleCompletion completion, @NonNull Throwable error, boolean async)
	{
		if(completion != null)
			completion.executeWithError(error, async);
	}
	
	public static void executeCompletion(@Nullable Blocks.SimpleCompletion completion)
	{
		if(completion != null)
			completion.execute();
	}
	
	public static void executeCompletion(@Nullable Blocks.SimpleCompletion completion, boolean async)
	{
		if(completion != null)
			completion.execute(async);
	}
	
	public static <T> void executeCompletionBlock(@Nullable Blocks.CompletionBlock<T> completion, @NonNull Throwable error)
	{
		if(completion != null)
			completion.execute(false, null, error);
	}
	
	public static <T> void executeCompletionBlock(@Nullable Blocks.CompletionBlock<T> completion, @NonNull T result)
	{
		if(completion != null)
			completion.execute(true, result, null);
	}
	
	public static void executeCompletionBlock(@Nullable Blocks.SimpleCompletionBlock completion, @NonNull Throwable error)
	{
		if(completion != null)
			completion.execute(false, error);
	}
	
	public static void executeCompletionBlock(@Nullable Blocks.SimpleCompletionBlock completion)
	{
		if(completion != null)
			completion.execute(true, null);
	}
	
	public static @Nullable <T> Blocks.CompletionBlock<T> unwrapCompletionBlock(@Nullable Blocks.Completion<T> completion)
	{
		return (completion == null) ? null : completion.getBlock();
	}
	
	public static @Nullable Blocks.SimpleCompletionBlock unwrapCompletionBlock(@Nullable Blocks.SimpleCompletion completion)
	{
		return (completion == null) ? null : completion.getBlock();
	}
	
	public static @Nullable <T> Blocks.Completion<T> wrapCompletionBlock(@Nullable Blocks.CompletionBlock<T> completionBlock)
	{
		return (completionBlock == null) ? null : new Blocks.Completion<>(completionBlock);
	}
	
	public static @Nullable Blocks.SimpleCompletion wrapCompletionBlock(@Nullable Blocks.SimpleCompletionBlock completionBlock)
	{
		return (completionBlock == null) ? null : new Blocks.SimpleCompletion(completionBlock);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Block
	{
		void execute();
	}
	
	public interface BlockWithArray <T>
	{
		void execute(T[] objects);
	}
	
	public interface BlockWithBoolean
	{
		void execute(boolean value);
	}
	
	public interface BlockWithError
	{
		void execute(Throwable error);
	}
	
	public interface BlockWithObject <T>
	{
		void execute(T object);
	}
	
	public interface CompletionBlock <T>
	{
		void execute(boolean succeeded, @Nullable T result, @Nullable Throwable error);
	}
	
	public interface SimpleCompletionBlock
	{
		void execute(boolean succeeded, @Nullable Throwable error);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	public static class Completion <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Execution
		
		private @NonNull CompletionBlock<T> _block;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties accessors - Execution
		
		public @NonNull CompletionBlock<T> getBlock()
		{
			return _block;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory management
		
		public Completion(@NonNull CompletionBlock<T> block)
		{
			_block = block;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Execution management
		
		public void executeWithError(@NonNull Throwable error)
		{
			this.executeWithError(error, false);
		}
		
		public void executeWithError(@NonNull Throwable error, boolean async)
		{
			CompletionBlock<T> block = this.getBlock();
			
			if(!async)
			{
				block.execute(false, null, error);
				return;
			}
			
			this.executeWithError(error, OperationQueue.getBackgroundQueue());
		}
		
		public void executeWithError(final @NonNull Throwable error, @NonNull OperationQueue queue)
		{
			final CompletionBlock<T> block = this.getBlock();
			
			queue.addOperation(new Block()
			{
				@Override public void execute()
				{
					block.execute(false, null, error);
				}
			});
		}
		
		public void executeWithResult(@NonNull T result)
		{
			this.executeWithResult(result, false);
		}
		
		public void executeWithResult(@NonNull T result, boolean async)
		{
			CompletionBlock<T> block = this.getBlock();
			
			if(!async)
			{
				block.execute(true, result, null);
				return;
			}
			
			this.executeWithResult(result, OperationQueue.getBackgroundQueue());
		}
		
		public void executeWithResult(@NonNull final T result, @NonNull OperationQueue queue)
		{
			final CompletionBlock<T> block = this.getBlock();
			
			queue.addOperation(new Block()
			{
				@Override public void execute()
				{
					block.execute(true, result, null);
				}
			});
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public static class SimpleCompletion
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Execution
		
		private @NonNull SimpleCompletionBlock _block;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties accessors - Execution
		
		public @NonNull SimpleCompletionBlock getBlock()
		{
			return _block;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory management
		
		public SimpleCompletion(@NonNull SimpleCompletionBlock block)
		{
			_block = block;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Execution management
		
		public void execute()
		{
			this.execute(false);
		}
		
		public void execute(boolean async)
		{
			SimpleCompletionBlock block = this.getBlock();
			
			if(!async)
			{
				block.execute(true, null);
				return;
			}
			
			this.execute(OperationQueue.getBackgroundQueue());
		}
		
		public void execute(@NonNull OperationQueue queue)
		{
			final SimpleCompletionBlock block = this.getBlock();
			
			queue.addOperation(new Block()
			{
				@Override public void execute()
				{
					block.execute(true, null);
				}
			});
		}
		
		public void executeWithError(@NonNull Throwable error)
		{
			this.executeWithError(error, false);
		}
		
		public void executeWithError(@NonNull Throwable error, boolean async)
		{
			SimpleCompletionBlock block = this.getBlock();
			
			if(!async)
			{
				block.execute(false, error);
				return;
			}
			
			this.executeWithError(error, OperationQueue.getBackgroundQueue());
		}
		
		public void executeWithError(final @NonNull Throwable error, @NonNull OperationQueue queue)
		{
			final SimpleCompletionBlock block = this.getBlock();
			
			queue.addOperation(new Block()
			{
				@Override public void execute()
				{
					block.execute(false, error);
				}
			});
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
