//
//	The MIT License (MIT)
//
//	Copyright © 2018-2019 Jacopo Filié
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

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.NonNull;

public abstract class Images
{
	public static @NonNull Bitmap resizeImage(@NonNull Bitmap source, @NonNull Geometry.Size maxSize)
	{
		float maxWidth = maxSize.getWidth();
		float maxHeight = maxSize.getHeight();
		
		if((maxWidth == 0) || (maxHeight == 0))
			return source;
		
		float width = source.getWidth();
		float height = source.getHeight();
		
		if((width <= maxWidth) && (height <= maxHeight))
			return source;
		
		float ratio = width / height;
		if(ratio > 1)
		{
			width = maxWidth;
			height = width / ratio;
		}
		else
		{
			height = maxHeight;
			width = height * ratio;
		}
		
		Bitmap retObj = Bitmap.createScaledBitmap(source, (int)width, (int)height, false);
		return ((retObj == null) ? source : retObj);
	}
	
	public static @NonNull Bitmap rotateImage(@NonNull Bitmap source, float rotation)
	{
		if(Float.compare(rotation, 0.0f) == 0)
			return source;
		
		Matrix matrix = new Matrix();
		matrix.preRotate(rotation);
		
		Bitmap retObj = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
		return ((retObj == null) ? source : retObj);
	}
}
