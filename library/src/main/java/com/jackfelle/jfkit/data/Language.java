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

import com.jackfelle.jfkit.utilities.Utilities;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Language
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private @NonNull String _code /* ISO 639-1 */;
	private @NonNull String _countryCode /* ISO 3166-1 */;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Data
	
	public @NonNull String getCode()
	{
		return _code;
	}
	
	public @NonNull String getCountryCode()
	{
		return _countryCode;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public Language(@NonNull String code, @NonNull String countryCode)
	{
		super();
		
		_code = code.toLowerCase();
		_countryCode = countryCode.toUpperCase();
	}
	
	@Override public boolean equals(@Nullable Object object)
	{
		if(!(object instanceof Language))
			return false;
		
		if(object == this)
			return true;
		
		Language other = (Language)object;
		
		if(!Utilities.areObjectsEqual(this.getCode(), other.getCode()))
			return false;
		
		return Utilities.areObjectsEqual(this.getCountryCode(), other.getCountryCode());
	}
	
	@Override public int hashCode()
	{
		return this.getCode().hashCode() + this.getCountryCode().hashCode();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data
	
	@Override public String toString()
	{
		return String.format(Locale.US, "%s_%s", this.getCode(), this.getCountryCode());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
