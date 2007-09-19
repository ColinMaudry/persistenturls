package org.purl.accessor.command;

/**
 *=========================================================================
 *
 *  Copyright (C) 2007 OCLC (http://oclc.org)
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *=========================================================================
 *
 */

import java.util.Iterator;

import org.purl.accessor.NKHelper;
import org.purl.accessor.ResourceFilter;
import org.purl.accessor.ResourceStorage;
import org.purl.accessor.URIResolver;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFResponse;
import org.ten60.netkernel.layer1.nkf.NKFException;
import org.ten60.netkernel.layer1.representation.IAspectNVP;
import org.ten60.netkernel.xml.representation.IAspectXDA;
import org.ten60.netkernel.xml.xda.IXDAReadOnly;
import org.ten60.netkernel.xml.xda.IXDAReadOnlyIterator;
import org.ten60.netkernel.xml.xda.XPathLocationException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.IURRepresentation;
import com.ten60.netkernel.urii.aspect.IAspectString;
import com.ten60.netkernel.urii.aspect.StringAspect;

public class GetResourceCommand extends PURLCommand {

    private ResourceFilter filter;

    public GetResourceCommand(String type, URIResolver uriResolver, ResourceStorage resStorage) {
        this(type, uriResolver, resStorage, null);
    }

    public GetResourceCommand(String type, URIResolver uriResolver, ResourceStorage resStorage, ResourceFilter filter) {
        super(type, uriResolver, resStorage);
        this.filter = filter;
    }

    @Override
    public INKFResponse execute(INKFConvenienceHelper context) {
        INKFResponse retValue = null;

        try {
            String path = context.getThisRequest().getArgument("path");

            System.out.println("path:" + path);

            if(!path.endsWith("/")) {
                String id = NKHelper.getLastSegment(context);
                if(resStorage.resourceExists(context, uriResolver)) {
                    IURAspect asp = resStorage.getResource(context, uriResolver);

                    // Filter the response if we have a filter
                    if(filter!=null) {
                        asp = filter.filter(context, asp);
                    }

                    // Default response code of 200 is fine
                    IURRepresentation rep = NKHelper.setResponseCode(context, asp, 200);
                    rep = NKHelper.attachGoldenThread(context, "gt:" + type + ":" + id , rep);
                    retValue = context.createResponseFrom(rep);
                    retValue.setCacheable();
                    retValue.setMimeType(NKHelper.MIME_XML);
                } else {
                    IURRepresentation rep = NKHelper.setResponseCode(context, new StringAspect("No such resource: " + id), 404);
                    retValue = context.createResponseFrom(rep);
                    retValue.setMimeType(NKHelper.MIME_TEXT);
                }
            } else {
                IAspectNVP params = (IAspectNVP) context.sourceAspect( "this:param:param", IAspectNVP.class);
                Iterator<String> namesItor = params.getNames().iterator();

                StringBuffer sb = new StringBuffer();

                while(namesItor.hasNext()) {
                    String key = namesItor.next();
                    String value = params.getValue(key);

                    if(!value.equals("")) {
                        if(sb.length() > 0) {
                            sb.append(" and " );
                        }

                        sb.append("(" + value + " and basis:/" + type + "/" + key + ")\n");
                    }
                }

                System.out.println(sb.toString());

                IURRepresentation rep = NKHelper.search(context, "ffcpl:/index/purls", sb.toString());

                IAspectString searchResults = (IAspectString) context.transrept(rep, IAspectString.class);
                System.out.println(searchResults.getString());
                IAspectXDA searchXDA = (IAspectXDA) context.transrept(rep, IAspectXDA.class);
                IXDAReadOnly roSearchXDA = searchXDA.getXDA();

                sb = new StringBuffer("<results>");

                try {
                    IXDAReadOnlyIterator roXDAItor = roSearchXDA.readOnlyIterator("//docid");
                    while(roXDAItor.hasNext()) {
                        String str = roXDAItor.getCurrentXPath();
                        String uri = roSearchXDA.getText(str, true);

                        IURAspect iur = resStorage.getResource(context, uri);
                        if(iur != null) {
                            StringAspect sa = (StringAspect) context.transrept(iur, IAspectString.class);
                            sb.append(sa.getString());
                        }

                        roXDAItor.next();
                    }
                } catch (XPathLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                sb.append("</results>");

                System.out.println(sb.toString());

                retValue = context.createResponseFrom(new StringAspect(sb.toString()));
                retValue.setMimeType(NKHelper.MIME_XML);
            }

        } catch (NKFException e) {
            // TODO Handle
            e.printStackTrace();
        }

        return retValue;
    }
}
