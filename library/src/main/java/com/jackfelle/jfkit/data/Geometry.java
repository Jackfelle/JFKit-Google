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

import com.jackfelle.jfkit.utilities.Utilities;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Geometry
{
	public static class EdgeInsets implements MutableCopying
	{
		protected float _bottom;
		protected float _left;
		protected float _right;
		protected float _top;
		
		public float getBottom()
		{
			return _bottom;
		}
		
		public float getLeft()
		{
			return _left;
		}
		
		public float getRight()
		{
			return _right;
		}
		
		public float getTop()
		{
			return _top;
		}
		
		public EdgeInsets(float left, float top, float right, float bottom)
		{
			_bottom = bottom;
			_left = left;
			_right = right;
			_top = top;
		}
		
		public EdgeInsets(float inset)
		{
			this(inset, inset, inset, inset);
		}
		
		protected EdgeInsets(@NonNull EdgeInsets source)
		{
			this(source.getLeft(), source.getTop(), source.getRight(), source.getBottom());
		}
		
		@Override public boolean equals(@Nullable Object obj)
		{
			if(obj == this)
				return true;
			
			if((obj == null) || !(obj instanceof EdgeInsets))
				return false;
			
			EdgeInsets object = (EdgeInsets)obj;
			
			if(Float.compare(_bottom, object._bottom) != 0)
				return false;
			
			if(Float.compare(_left, object._left) != 0)
				return false;
			
			if(Float.compare(_right, object._right) != 0)
				return false;
			
			return (Float.compare(_top, object._top) == 0);
		}
		
		@Override public int hashCode()
		{
			return Utilities.hashCode(_bottom, _left, _right, _top);
		}
		
		@Override public String toString()
		{
			return String.format(Locale.US, "%s{%g, %g, %g, %g}", this.getClass().getSimpleName(), this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
		}
		
		@Override public @NonNull EdgeInsets copy()
		{
			return this;
		}
		
		@Override public @NonNull MutableEdgeInsets mutableCopy()
		{
			return new MutableEdgeInsets(this);
		}
	}
	
	public static class MutableEdgeInsets extends EdgeInsets
	{
		public void setBottom(float bottom)
		{
			_bottom = bottom;
		}
		
		public void setLeft(float left)
		{
			_left = left;
		}
		
		public void setRight(float right)
		{
			_right = right;
		}
		
		public void setTop(float top)
		{
			_top = top;
		}
		
		public MutableEdgeInsets(float left, float top, float right, float bottom)
		{
			super(left, top, right, bottom);
		}
		
		public MutableEdgeInsets(float padding)
		{
			super(padding);
		}
		
		protected MutableEdgeInsets(@NonNull EdgeInsets source)
		{
			super(source);
		}
		
		@Override public @NonNull EdgeInsets copy()
		{
			return new EdgeInsets(this);
		}
		
		@Override public @NonNull MutableEdgeInsets mutableCopy()
		{
			try
			{
				return (MutableEdgeInsets)super.clone();
			}
			catch(CloneNotSupportedException exception)
			{
				return new MutableEdgeInsets(this);
			}
		}
	}
	
	public static class Point implements MutableCopying
	{
		protected float _x;
		protected float _y;
		
		public float getX()
		{
			return _x;
		}
		
		public float getY()
		{
			return _y;
		}
		
		public Point(float x, float y)
		{
			_x = x;
			_y = y;
		}
		
		protected Point(@NonNull Point source)
		{
			this(source.getX(), source.getY());
		}
		
		@Override public boolean equals(@Nullable Object obj)
		{
			if(obj == this)
				return true;
			
			if((obj == null) || !(obj instanceof Point))
				return false;
			
			Point object = (Point)obj;
			
			if(Float.compare(_x, object._x) != 0)
				return false;
			
			return (Float.compare(_y, object._y) == 0);
		}
		
		@Override public int hashCode()
		{
			return Utilities.hashCode(_x, _y);
		}
		
		@Override public String toString()
		{
			return String.format(Locale.US, "%s{%g, %g}", this.getClass().getSimpleName(), this.getX(), this.getY());
		}
		
		@Override public @NonNull Point copy()
		{
			return this;
		}
		
		@Override public @NonNull Object mutableCopy()
		{
			return new MutablePoint(this);
		}
	}
	
	public static class Point3D extends Point implements MutableCopying
	{
		protected float _z;
		
		public float getZ()
		{
			return _z;
		}
		
		public Point3D(float x, float y, float z)
		{
			super(x, y);
			
			_z = z;
		}
		
		protected Point3D(@NonNull Point3D source)
		{
			this(source.getX(), source.getY(), source.getZ());
		}
		
		@Override public boolean equals(@Nullable Object obj)
		{
			if(obj == this)
				return true;
			
			if((obj == null) || !super.equals(obj) || !(obj instanceof Point3D))
				return false;
			
			Point3D object = (Point3D)obj;
			
			return (Float.compare(_z, object._z) == 0);
		}
		
		@Override public int hashCode()
		{
			return Utilities.hashCode(super.hashCode(), _z);
		}
		
		@Override public String toString()
		{
			return String.format(Locale.US, "%s{%g, %g, %g}", this.getClass().getSimpleName(), this.getX(), this.getY(), this.getZ());
		}
		
		@Override public @NonNull Point3D copy()
		{
			return this;
		}
		
		public @NonNull Object mutableCopy()
		{
			return new MutablePoint3D(this);
		}
	}
	
	public static class MutablePoint extends Point
	{
		public void setX(float x)
		{
			_x = x;
		}
		
		public void setY(float y)
		{
			_y = y;
		}
		
		public MutablePoint(float x, float y)
		{
			super(x, y);
		}
		
		protected MutablePoint(@NonNull Point source)
		{
			super(source);
		}
		
		@Override public @NonNull Point copy()
		{
			return new Point(this);
		}
		
		@Override public @NonNull MutablePoint mutableCopy()
		{
			try
			{
				return (MutablePoint)super.clone();
			}
			catch(CloneNotSupportedException exception)
			{
				return new MutablePoint(this);
			}
		}
	}
	
	public static class MutablePoint3D extends Point3D
	{
		public void setX(float x)
		{
			_x = x;
		}
		
		public void setY(float y)
		{
			_y = y;
		}
		
		public void setZ(float z)
		{
			_z = z;
		}
		
		public MutablePoint3D(float x, float y, float z)
		{
			super(x, y, z);
		}
		
		protected MutablePoint3D(@NonNull Point3D source)
		{
			super(source);
		}
		
		@Override public @NonNull Point3D copy()
		{
			return new Point3D(this);
		}
		
		@Override public @NonNull Object mutableCopy()
		{
			try
			{
				return (MutablePoint3D)super.clone();
			}
			catch(CloneNotSupportedException exception)
			{
				return new MutablePoint3D(this);
			}
		}
	}
	
	public static class Rect implements MutableCopying
	{
		protected @NonNull Point _origin;
		protected @NonNull Size _size;
		
		public @NonNull Point getBottomLeftVertex()
		{
			Point origin = this.getOrigin();
			return new Point(origin.getX(), origin.getY() + this.getSize().getHeight());
		}
		
		public @NonNull Point getBottomRightVertex()
		{
			Point origin = this.getOrigin();
			Size size = this.getSize();
			return new Point(origin.getX() + size.getWidth(), origin.getY() + size.getHeight());
		}
		
		public @NonNull Point getCenter()
		{
			Point origin = this.getOrigin();
			Size size = this.getSize();
			return new Point(origin.getX() + size.getWidth() / 2.0f, origin.getY() + size.getHeight() / 2.0f);
		}
		
		public @NonNull Point getOrigin()
		{
			return _origin;
		}
		
		public @NonNull Size getSize()
		{
			return _size;
		}
		
		public @NonNull Point getTopLeftVertex()
		{
			return this.getOrigin().copy();
		}
		
		public @NonNull Point getTopRightVertex()
		{
			Point origin = this.getOrigin();
			return new Point(origin.getX() + this.getSize().getWidth(), origin.getY());
		}
		
		public Rect(@NonNull Point origin, @NonNull Size size)
		{
			_origin = origin;
			_size = size;
		}
		
		protected Rect(@NonNull Rect source)
		{
			this(source.getOrigin(), source.getSize());
		}
		
		@Override public boolean equals(@Nullable Object obj)
		{
			if(obj == this)
				return true;
			
			if((obj == null) || !(obj instanceof Rect))
				return false;
			
			Rect object = (Rect)obj;
			
			if(!Utilities.areObjectsEqual(_origin, object._origin))
				return false;
			
			return (Utilities.areObjectsEqual(_size, object._size));
		}
		
		@Override public int hashCode()
		{
			return Utilities.hashCode(_origin, _size);
		}
		
		@Override public String toString()
		{
			return String.format(Locale.US, "%s{%s, %s}", this.getClass().getSimpleName(), this.getOrigin().toString(), this.getSize().toString());
		}
		
		@Override public @NonNull Rect copy()
		{
			return this;
		}
		
		@Override public @NonNull MutableRect mutableCopy()
		{
			return new MutableRect(this);
		}
	}
	
	public static class MutableRect extends Rect
	{
		public void setOrigin(@NonNull Point origin)
		{
			_origin = origin;
		}
		
		public void setSize(@NonNull Size size)
		{
			_size = size;
		}
		
		public MutableRect(@NonNull Point origin, @NonNull Size size)
		{
			super(origin, size);
		}
		
		protected MutableRect(@NonNull Rect source)
		{
			super(source);
		}
		
		@Override public @NonNull Rect copy()
		{
			return new Rect(this);
		}
		
		@Override public @NonNull MutableRect mutableCopy()
		{
			try
			{
				return (MutableRect)super.clone();
			}
			catch(CloneNotSupportedException exception)
			{
				return new MutableRect(this);
			}
		}
	}
	
	public static class Size implements MutableCopying
	{
		protected float _height;
		protected float _width;
		
		public float getHeight()
		{
			return _height;
		}
		
		public float getWidth()
		{
			return _width;
		}
		
		public Size(float width, float height)
		{
			_width = width;
			_height = height;
		}
		
		protected Size(@NonNull Size source)
		{
			this(source.getWidth(), source.getWidth());
		}
		
		@Override public boolean equals(@Nullable Object obj)
		{
			if(obj == this)
				return true;
			
			if((obj == null) || !(obj instanceof Size))
				return false;
			
			Size object = (Size)obj;
			
			if(Float.compare(_width, object._width) != 0)
				return false;
			
			return (Float.compare(_height, object._height) == 0);
		}
		
		@Override public int hashCode()
		{
			return Utilities.hashCode(_height, _width);
		}
		
		@Override public String toString()
		{
			return String.format(Locale.US, "%s{%g, %g}", this.getClass().getSimpleName(), this.getWidth(), this.getHeight());
		}
		
		@Override public @NonNull Size copy()
		{
			return this;
		}
		
		@Override public @NonNull MutableSize mutableCopy()
		{
			return new MutableSize(this);
		}
	}
	
	public static class MutableSize extends Size
	{
		public void setHeight(float height)
		{
			_height = height;
		}
		
		public void setWidth(float width)
		{
			_width = width;
		}
		
		public MutableSize(float width, float height)
		{
			super(width, height);
		}
		
		protected MutableSize(@NonNull Size source)
		{
			super(source);
		}
		
		@Override public @NonNull Size copy()
		{
			return new Size(this);
		}
		
		@Override public @NonNull MutableSize mutableCopy()
		{
			try
			{
				return (MutableSize)super.clone();
			}
			catch(CloneNotSupportedException exception)
			{
				return new MutableSize(this);
			}
		}
	}
	
	public static final @NonNull EdgeInsets EDGE_INSETS_ZERO = new EdgeInsets(0, 0, 0, 0);
	
	public static final @NonNull Point POINT_ZERO = new Point(0, 0);
	public static final @NonNull Size SIZE_ZERO = new Size(0, 0);
	
	public static final @NonNull Rect RECT_ZERO = new Rect(POINT_ZERO, SIZE_ZERO);
}
