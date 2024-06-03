package com.mockrunner.example.ejb;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.mockrunner.example.ejb.interfaces.LogSession;
import com.mockrunner.example.ejb.interfaces.LogSessionHome;

/**
 * This example action takes a message parameter from the request
 * and uses the {@link LogSessionBean} to log the message.
 * See LogActionTest for an example test of this action.
 */
public class LogAction extends Action
{
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception
    {
        String message = request.getParameter("message");
        try 
        {
            if(null != message)
            {
                InitialContext initialContext = new InitialContext();
                Object home = initialContext.lookup("com/mockrunner/example/LogSession");
                LogSessionHome logHome = (LogSessionHome) PortableRemoteObject.narrow(home, LogSessionHome.class);
                LogSession log = logHome.create();
                log.logMessage(message);
                log.remove();
            }
        } 
        catch(Exception exc)
        {
            exc.printStackTrace();
        }
        return mapping.findForward("success");
    }
}
