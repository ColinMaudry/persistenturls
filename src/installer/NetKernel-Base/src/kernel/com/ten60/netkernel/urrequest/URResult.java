/******************************************************************************
 * (c) Copyright 2002,2003, 1060 Research Ltd
 *
 * This Software is licensed to You, the licensee, for use under the terms of
 * the 1060 Public License v1.0. Please read and agree to the 1060 Public
 * License v1.0 [www.1060research.com/license] before using or redistributing
 * this software.
 *
 * In summary the 1060 Public license has the following conditions.
 * A. You may use the Software free of charge provided you agree to the terms
 * laid out in the 1060 Public License v1.0
 * B. You are only permitted to use the Software with components or applications
 * that provide you with OSI Certified Open Source Code [www.opensource.org], or
 * for which licensing has been approved by 1060 Research Limited.
 * You may write your own software for execution by this Software provided any
 * distribution of your software with this Software complies with terms set out
 * in section 2 of the 1060 Public License v1.0
 * C. You may redistribute the Software provided you comply with the terms of
 * the 1060 Public License v1.0 and that no warranty is implied or given.
 * D. If you find you are unable to comply with this license you may seek to
 * obtain an alternative license from 1060 Research Limited by contacting
 * license@1060research.com or by visiting www.1060research.com
 *
 * NO WARRANTY:  THIS SOFTWARE IS NOT COVERED BY ANY WARRANTY. SEE 1060 PUBLIC
 * LICENSE V1.0 FOR DETAILS
 *
 * THIS COPYRIGHT NOTICE IS *NOT* THE 1060 PUBLIC LICENSE v1.0. PLEASE READ
 * THE DISTRIBUTED 1060_Public_License.txt OR www.1060research.com/license
 *
 * File:          $RCSfile: URResult.java,v $
 * Version:       $Name:  $ $Revision: 1.1 $
 * Last Modified: $Date: 2004/06/18 10:15:23 $
 *****************************************************************************/
package com.ten60.netkernel.urrequest;

import com.ten60.netkernel.urii.*;
/**
 * The result of a request on a requestee
 * @author  tab
 */
public final class URResult
{
	/** the request that was made **/
	private URRequest mRequest;
	/** the representation of the resulting resource */
	private IURRepresentation mResource;
	
	/** Construct a new URResult
	 * @param aRequest the request that this is the result for
	 * @param aResource the resource that is the result of the request
	 */
	public URResult(URRequest aRequest, IURRepresentation aResource)
	{	mRequest = aRequest;
		mResource = aResource;
	}
	/** return the request that this is the result for
	 */
    public URRequest getRequest()
	{	return mRequest;
	}
	/* return the resource that is the result of the request
	 */
    public IURRepresentation getResource()
	{	return mResource;
	}
}