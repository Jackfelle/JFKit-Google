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

import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.jackfelle.jfkit.data.DeepEquality;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Utilities
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Arrays
	
	public static @NonNull <T> ArrayList<T> getAsArrayList(@NonNull Collection<T> collection)
	{
		return (collection instanceof ArrayList) ? (ArrayList<T>)collection : new ArrayList<>(collection);
	}
	
	public static @NonNull <T> LinkedList<T> getAsLinkedList(@NonNull Collection<T> collection)
	{
		return (collection instanceof LinkedList) ? (LinkedList<T>)collection : new LinkedList<>(collection);
	}
	
	public static @NonNull <K, V> Map<K, V> getAsMap(@NonNull Collection<V> collection, @NonNull MapKeyGetter<K, V> keyGetter)
	{
		Map<K, V> retObj = new HashMap<>();
		for(V value : collection)
			retObj.put(keyGetter.getKeyForValue(value), value);
		return retObj;
	}
	
	public static @Nullable <T> T getFirstArrayItem(@Nullable T[] array)
	{
		return ((array != null) && (array.length > 0)) ? array[0] : null;
	}
	
	public static @Nullable <T> T getFirstListItem(@Nullable List<T> list)
	{
		return ((list != null) && (list.size() > 0)) ? list.get(0) : null;
	}
	
	public static @Nullable <T> T getLastArrayItem(@Nullable T[] array)
	{
		int length = (array == null) ? 0 : array.length;
		return (length > 0) ? array[length - 1] : null;
	}
	
	public static @Nullable <T> T getLastListItem(@Nullable List<T> list)
	{
		int size = (list == null) ? 0 : list.size();
		return (size > 0) ? list.get(size - 1) : null;
	}
	
	public static <T> boolean isInArray(@Nullable T[] array, @Nullable T object)
	{
		if((array == null) || (object == null))
			return false;
		
		for(T item : array)
		{
			if(item.equals(object))
				return true;
		}
		
		return false;
	}
	
	public static @NonNull <K, V> Map<K, V> newMapWithDefaultValues(@NonNull Collection<K> keys, @NonNull V defVal)
	{
		Map<K, V> retObj = new HashMap<>();
		for(K key : keys)
			retObj.put(key, defVal);
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Colors
	
	public static @Nullable @ColorInt Integer parseColorHexString(@Nullable String hexString)
	{
		if(hexString == null)
			return null;
		
		if(!hexString.startsWith("#"))
			hexString = "#" + hexString;
		try
		{
			return Color.parseColor(hexString);
		}
		catch(IllegalArgumentException exception)
		{
			return null;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Comparison
	
	public static <T extends DeepEquality> boolean areArraysDeeplyEqual(@Nullable T[] array1, @Nullable T[] array2)
	{
		// If both are 'null', they are equal.
		if((array1 == null) && (array2 == null))
			return true;
		
		// If anyone is still equal to 'null', they can't be equal.
		if((array1 == null) || (array2 == null))
			return false;
		
		// If they haven't the same length, they can't be equal.
		int length = array1.length;
		if(length != array2.length)
			return false;
		
		if(length == 0)
			return true;
		
		for(int i = 0; i < length; i++)
		{
			if(!Utilities.areObjectsDeeplyEqual(array1[i], array2[i]))
				return false;
		}
		
		return true;
	}
	
	public static <T extends DeepEquality> boolean areListsDeeplyEqual(@Nullable List<T> list1, @Nullable List<T> list2)
	{
		// If both are 'null', they are equal.
		if((list1 == null) && (list2 == null))
			return true;
		
		// If anyone is still equal to 'null', they can't be equal.
		if((list1 == null) || (list2 == null))
			return false;
		
		// If they haven't the same size, they can't be equal.
		int size = list1.size();
		if(size != list2.size())
			return false;
		
		if(size == 0)
			return true;
		
		for(int i = 0; i < size; i++)
		{
			if(!Utilities.areObjectsDeeplyEqual(list1.get(i), list2.get(i)))
				return false;
		}
		
		return true;
	}
	
	public static <T extends DeepEquality> boolean areObjectsDeeplyEqual(@Nullable T obj1, @Nullable T obj2)
	{
		// If both are 'null', they are equal.
		if((obj1 == null) && (obj2 == null))
			return true;
		
		// If anyone is still equal to 'null', they can't be equal.
		if((obj1 == null) || (obj2 == null))
			return false;
		
		return obj1.deeplyEquals(obj2);
	}
	
	public static boolean areObjectsEqual(@Nullable Object obj1, @Nullable Object obj2)
	{
		// If both are 'null', they are equal.
		if((obj1 == null) && (obj2 == null))
			return true;
		
		// If anyone is still equal to 'null', they can't be equal.
		if((obj1 == null) || (obj2 == null))
			return false;
		
		return obj1.equals(obj2);
	}
	
	public static <T extends DeepEquality> boolean areSetsDeeplyEqual(@Nullable Set<T> set1, @Nullable Set<T> set2)
	{
		// If both are 'null', they are equal.
		if((set1 == null) && (set2 == null))
			return true;
		
		// If anyone is still equal to 'null', they can't be equal.
		if((set1 == null) || (set2 == null))
			return false;
		
		// If they haven't the same size, they can't be equal.
		int size = set1.size();
		if(size != set2.size())
			return false;
		
		if(size == 0)
			return true;
		
		for(T obj1 : set1)
		{
			if(!set2.contains(obj1))
				return false;
			
			boolean failed = true;
			for(T obj2 : set2)
			{
				if(Utilities.areObjectsDeeplyEqual(obj1, obj2))
				{
					failed = false;
					break;
				}
			}
			if(failed)
				return false;
		}
		
		return true;
	}
	
	public static int hashCode(@NonNull Object... objects)
	{
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
			return Objects.hash(objects);
		return Arrays.hashCode(objects);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Parcelable
	
	public static boolean readBooleanFromParcel(@NonNull Parcel parcel)
	{
		return (parcel.readInt() != 0);
	}
	
	public static @Nullable Integer readOptionalIntegerFromParcel(@NonNull Parcel parcel)
	{
		Integer retObj = null;
		if(Utilities.readBooleanFromParcel(parcel))
			retObj = parcel.readInt();
		return retObj;
	}
	
	public static @Nullable String readOptionalStringFromParcel(@NonNull Parcel parcel)
	{
		String retObj = null;
		if(Utilities.readBooleanFromParcel(parcel))
			retObj = parcel.readString();
		return retObj;
	}
	
	public static @Nullable <T extends Parcelable> List<T> readOptionalTypedListFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator)
	{
		List<T> retObj = null;
		if(Utilities.readBooleanFromParcel(parcel))
		{
			retObj = new ArrayList<>();
			parcel.readTypedList(retObj, creator);
		}
		return retObj;
	}
	
	public static @Nullable <T extends Parcelable> T readOptionalTypedObjectFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator)
	{
		T retObj = null;
		if(Utilities.readBooleanFromParcel(parcel))
		{
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
				retObj = parcel.readTypedObject(creator);
			else
				retObj = creator.createFromParcel(parcel);
		}
		return retObj;
	}
	
	public static @Nullable <T extends Parcelable> Set<T> readOptionalTypedSetFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator)
	{
		Set<T> retObj = null;
		if(Utilities.readBooleanFromParcel(parcel))
		{
			List<T> list = new ArrayList<>();
			parcel.readTypedList(list, creator);
			retObj = new HashSet<>(list);
		}
		return retObj;
	}
	
	public static @Nullable Uri readOptionalURIFromParcel(@NonNull Parcel parcel)
	{
		Uri retObj = null;
		if(Utilities.readBooleanFromParcel(parcel))
			retObj = Uri.parse(parcel.readString());
		return retObj;
	}
	
	public static void writeBooleanToParcel(@NonNull Parcel parcel, boolean value)
	{
		parcel.writeInt(value ? 1 : 0);
	}
	
	public static void writeOptionalIntegerToParcel(@NonNull Parcel parcel, @Nullable Integer value)
	{
		boolean valid = (value != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(valid)
			parcel.writeInt(value);
	}
	
	public static void writeOptionalStringToParcel(@NonNull Parcel parcel, @Nullable String string)
	{
		boolean valid = (string != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(valid)
			parcel.writeString(string);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedListToParcel(@NonNull Parcel parcel, @Nullable List<T> list)
	{
		boolean valid = (list != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(valid)
			parcel.writeTypedList(list);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedObjectToParcel(@NonNull Parcel parcel, @Nullable T object, int parcelableFlags)
	{
		boolean valid = (object != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(!valid)
			return;
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
			parcel.writeTypedObject(object, parcelableFlags);
		else
			object.writeToParcel(parcel, parcelableFlags);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedSetToParcel(@NonNull Parcel parcel, @Nullable Set<T> set)
	{
		boolean valid = (set != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(valid)
			parcel.writeTypedList(new ArrayList<>(set));
	}
	
	public static void writeOptionalURIToParcel(@NonNull Parcel parcel, @Nullable Uri uri)
	{
		boolean valid = (uri != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(valid)
			parcel.writeString(uri.toString());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - References
	
	public static @Nullable <T> SoftReference<T> softWrapObject(@Nullable T object)
	{
		return (object == null) ? null : new SoftReference<>(object);
	}
	
	public static @Nullable <T> T unwrapObject(@Nullable Reference<T> reference)
	{
		return (reference == null) ? null : reference.get();
	}
	
	public static @Nullable <T> WeakReference<T> weakWrapObject(@Nullable T object)
	{
		return (object == null) ? null : new WeakReference<>(object);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Selection
	
	public static @NonNull <T> T replaceIfNull(@Nullable T object, @NonNull T replacement)
	{
		return (object == null) ? replacement : object;
	}
	
	public static @NonNull <T> T replaceIfNull(@Nullable T object, @NonNull ReplacementBuilder<T> builder)
	{
		return (object == null) ? builder.getReplacement() : object;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Types
	
	public static @Nullable <T> T filterByType(@Nullable Object object, @NonNull Class<T> type)
	{
		if(type.isInstance(object))
			return type.cast(object);
		return null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface MapKeyGetter <K, V>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		@NonNull K getKeyForValue(@NonNull V value);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public interface ReplacementBuilder <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		@NonNull T getReplacement();
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
