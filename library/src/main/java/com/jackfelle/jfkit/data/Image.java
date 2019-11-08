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

package com.jackfelle.jfkit.data;

import android.graphics.Bitmap;

import com.jackfelle.jfkit.utilities.Utilities;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;

public class Image
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private @Nullable WeakReference<Bitmap> _image;
	private @Nullable ImageLoader _imageLoader;
	private @Nullable WeakReference<Bitmap> _thumbnail;
	private @Nullable ImageLoader _thumbnailLoader;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Data
	
	public @Nullable Bitmap getImage()
	{
		Bitmap retObj = Utilities.unwrapObject(_image);
		if(retObj == null)
		{
			synchronized(this)
			{
				retObj = Utilities.unwrapObject(_image);
				if(retObj == null)
				{
					retObj = this.loadImage();
					_image = Utilities.weakWrapObject(retObj);
				}
			}
		}
		return retObj;
	}
	
	public @Nullable ImageLoader getImageLoader()
	{
		return _imageLoader;
	}
	
	public @Nullable Bitmap getThumbnail()
	{
		Bitmap retObj = Utilities.unwrapObject(_thumbnail);
		if(retObj == null)
		{
			synchronized(this)
			{
				retObj = Utilities.unwrapObject(_thumbnail);
				if(retObj == null)
				{
					retObj = this.loadThumbnail();
					_thumbnail = Utilities.weakWrapObject(retObj);
				}
			}
		}
		return retObj;
	}
	
	public @Nullable ImageLoader getThumbnailLoader()
	{
		return _thumbnailLoader;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public Image(@Nullable ImageLoader imageLoader, @Nullable ImageLoader thumbnailLoader)
	{
		super();
		
		_imageLoader = imageLoader;
		_thumbnailLoader = thumbnailLoader;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data
	
	protected @Nullable Bitmap loadImage()
	{
		ImageLoader loader = this.getImageLoader();
		return (loader == null) ? null : loader.loadImage();
	}
	
	protected @Nullable Bitmap loadThumbnail()
	{
		ImageLoader loader = this.getThumbnailLoader();
		return (loader == null) ? null : loader.loadImage();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface ImageLoader
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		@Nullable Bitmap loadImage();
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
