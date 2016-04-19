package com.freshplanet.inapppurchase.functions;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.freshplanet.inapppurchase.Extension;

public class DisposeFunction extends BaseFunction
{
	@Override
	public FREObject call(FREContext context, FREObject[] args)
	{
		super.call(context, args);
		
		Extension.context.disposeIab();

		return null;	
	}
}