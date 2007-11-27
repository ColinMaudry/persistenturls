package org.purl.accessor;

import java.util.HashMap;
import java.util.Map;

import org.purl.accessor.command.CreateResourceCommand;
import org.purl.accessor.command.DeleteResourceCommand;
import org.purl.accessor.command.GetResourceCommand;
import org.purl.accessor.command.PURLCommand;
import org.purl.accessor.command.UpdateResourceCommand;
import org.purl.accessor.util.GroupResolver;
import org.purl.accessor.util.GroupResourceStorage;
import org.purl.accessor.util.PURLCreator;
import org.purl.accessor.util.PURLResourceStorage;
import org.purl.accessor.util.PURLURIResolver;
import org.purl.accessor.util.PurlSearchHelper;
import org.purl.accessor.util.ResourceCreator;
import org.purl.accessor.util.ResourceStorage;
import org.purl.accessor.util.URIResolver;
import org.purl.accessor.util.UserResolver;
import org.purl.accessor.util.UserResourceStorage;
import org.ten60.netkernel.layer1.nkf.INKFConvenienceHelper;
import org.ten60.netkernel.layer1.nkf.INKFRequest;
import org.ten60.netkernel.layer1.nkf.NKFException;

import com.ten60.netkernel.urii.IURAspect;
import com.ten60.netkernel.urii.aspect.IAspectString;

public class PURLAccessor extends AbstractAccessor {

    public static final String TYPE = "purl";

    private Map<String, PURLCommand> commandMap = new HashMap<String,PURLCommand>();

    private static URIResolver purlResolver;

    public PURLAccessor() {
        // We use stateless command instances that are triggered
        // based on the method of the HTTP request

        purlResolver = new PURLURIResolver();

        URIResolver userResolver = new UserResolver();
        URIResolver groupResolver = new GroupResolver();
        ResourceFilter purlFilter = new PURLPrivateDataFilter();

        ResourceCreator purlCreator = new PURLCreator(new URIResolver[] { userResolver, groupResolver }, purlResolver, new PURLResourceStorage(), new UserResourceStorage());
        ResourceStorage purlStorage = new PURLResourceStorage();
        ResourceStorage groupStorage = new GroupResourceStorage();

        commandMap.put("POST", new CreateResourceCommand(TYPE, purlResolver, purlCreator, purlFilter, purlStorage));
        commandMap.put("PUT", new UpdateResourceCommand(TYPE, purlResolver, purlCreator, purlStorage));
        commandMap.put("DELETE", new DeleteResourceCommand(TYPE, purlResolver, purlStorage));
        commandMap.put("GET", new GetResourceCommand(TYPE, purlResolver, purlStorage, new PurlSearchHelper(groupResolver, groupStorage), purlFilter));
    }

    protected PURLCommand getCommand(INKFConvenienceHelper context, String method) {
        return commandMap.get(method);
    }


    




    
    static public class PURLPrivateDataFilter implements ResourceFilter {

        public IURAspect filter(INKFConvenienceHelper context, IURAspect iur) {
            IURAspect retValue = null;

            try {
                INKFRequest req = context.createSubRequest();
                req.setURI("active:xslt");
                req.addArgument("operand", iur);
                req.addArgument("operator", "ffcpl:/filters/purl.xsl");
                req.setAspectClass(IAspectString.class);
                retValue = context.issueSubRequestForAspect(req);
            } catch(NKFException nfe) {
                // TODO: return something other than the raw user
                nfe.printStackTrace();
            }

            return retValue;
        }

    }
}
