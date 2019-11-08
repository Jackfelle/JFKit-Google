//
//	The MIT License (MIT)
//
//	Copyright © 2019 Jacopo Filié
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

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Optional <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Fields - Data
	
	private @NonNull Implementation<T> _implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	public @NonNull T get()
	{
		return this.getImplementation().getValue();
	}
	
	private @NonNull Implementation<T> getImplementation()
	{
		return _implementation;
	}
	
	public @Nullable T opt()
	{
		return this.getImplementation().optValue();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public static <T> Optional<T> newInstance(@NonNull Builder<T> builder)
	{
		return new Optional<>(new Implementation<>(builder));
	}
	
	public static <T> Optional<T> newSynchronizedInstance(@NonNull Builder<T> builder)
	{
		return new Optional<>(new SynchronizedImplementation<>(builder));
	}
	
	private Optional(@NonNull Implementation<T> implementation)
	{
		super();
		
		_implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Builder <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		@NonNull T build();
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Data
		
		private @NonNull Builder<T> _builder;
		protected @Nullable WeakReference<T> _value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties accessors - Data
		
		protected @NonNull Builder<T> getBuilder()
		{
			return _builder;
		}
		
		public @NonNull T getValue()
		{
			T retObj = this.optValue();
			if(retObj == null)
			{
				retObj = this.getBuilder().build();
				_value = Utilities.weakWrapObject(retObj);
			}
			return retObj;
		}
		
		public @Nullable T optValue()
		{
			return Utilities.unwrapObject(_value);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public Implementation(@NonNull Builder<T> builder)
		{
			super();
			
			_builder = builder;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T> extends Implementation<T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties accessors - Data
		
		@Override public @NonNull T getValue()
		{
			synchronized(this)
			{
				return super.getValue();
			}
		}
		
		@Override public @Nullable T optValue()
		{
			synchronized(this)
			{
				return super.optValue();
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public SynchronizedImplementation(@NonNull Builder<T> builder)
		{
			super(builder);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
