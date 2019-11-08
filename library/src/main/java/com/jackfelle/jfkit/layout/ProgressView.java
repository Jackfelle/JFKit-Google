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

package com.jackfelle.jfkit.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jackfelle.jfkit.R;
import com.jackfelle.jfkit.utilities.Utilities;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class ProgressView extends RelativeLayout
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private String _text;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - User interface (Layout)
	
	private View _alertView;
	private View _contentView;
	private ProgressBar _spinnerView;
	private TextView _textLabel;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties accessors - Data
	
	public String getText()
	{
		return _text;
	}
	
	public void setText(@StringRes int text)
	{
		this.setText(this.getContext().getString(text));
	}
	
	public void setText(String text)
	{
		if(Utilities.areObjectsEqual(_text, text))
			return;
		
		_text = text;
		
		this.updateLayout();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - User interface (Layout)
	
	protected View getAlertView()
	{
		return _alertView;
	}
	
	protected View getContentView()
	{
		return _contentView;
	}
	
	protected ProgressBar getSpinnerView()
	{
		return _spinnerView;
	}
	
	protected TextView getTextLabel()
	{
		return _textLabel;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory management
	
	private static void initialize(@NonNull Context context, @NonNull ProgressView object)
	{
		View contentView = View.inflate(context, R.layout.progress_view, object);
		
		object._alertView = contentView.findViewById(R.id.alert_view);
		object._contentView = contentView;
		object._spinnerView = contentView.findViewById(R.id.spinner_view);
		object._textLabel = contentView.findViewById(R.id.text_label);
		
		// This will block any touch that will try to pass through the content view.
		contentView.setOnTouchListener(new OnTouchListener()
		{
			@Override public boolean onTouch(View v, MotionEvent event)
			{
				return true;
			}
		});
		
		object.updateLayout();
	}
	
	public ProgressView(@NonNull Context context)
	{
		super(context);
		ProgressView.initialize(context, this);
	}
	
	public ProgressView(@NonNull Context context, AttributeSet attrs)
	{
		super(context, attrs);
		ProgressView.initialize(context, this);
	}
	
	public ProgressView(@NonNull Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		ProgressView.initialize(context, this);
	}
	
	@TargetApi(21) public ProgressView(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		ProgressView.initialize(context, this);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - User interface management
	
	protected void updateLayout()
	{
		TextView textLabel = this.getTextLabel();
		if(textLabel == null)
			return;
		
		String text = this.getText();
		textLabel.setText(text);
		textLabel.setVisibility((text == null) ? GONE : VISIBLE);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
