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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import androidx.annotation.NonNull;

public final class ObjectIdentifier
{
	private @NonNull static final AtomicLong _builder = new AtomicLong(0);
	private @NonNull static final Map<WeakReference<Object>, Long> _registry = new ConcurrentHashMap<>();
	
	public static long getID(@NonNull Object object)
	{
		Long retVal = null;
		
		List<WeakReference<Object>> oldReferences = null;
		for(WeakReference<Object> reference : _registry.keySet())
		{
			Object storedObject = reference.get();
			if(storedObject == null)
			{
				if(oldReferences == null)
					oldReferences = new ArrayList<>();
				oldReferences.add(reference);
				continue;
			}
			
			if(storedObject == object)
			{
				retVal = _registry.get(reference);
				break;
			}
		}
		
		if(oldReferences != null)
		{
			for(WeakReference<Object> reference : oldReferences)
				_registry.remove(reference);
		}
		
		if(retVal == null)
		{
			retVal = _builder.getAndIncrement();
			_registry.put(new WeakReference<>(object), retVal);
		}
		
		return retVal;
	}
}
