package org.quartz.jobs.ee.ejb;

import java.lang.reflect.InvocationTargetException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * A <code>Job</code> that invokes a method on an EJB3.
 * </p>
 * 
 * Expects the properties corresponding to the following keys to be in the
 * <code>JobDataMap</code> when it executes:
 * <ul>
 * <li><code>EJB_JNDI_NAME_KEY</code>- the JNDI name (location) of the EJB's
 * home interface.</li>
 * <li><code>EJB_METHOD_KEY</code>- the name of the method to invoke on the EJB.
 * </li>
 * <li><code>EJB_ARGS_KEY</code>- an Object[] of the args to pass to the method
 * (optional, if left out, there are no arguments).</li>
 * <li><code>EJB_ARG_TYPES_KEY</code>- an Class[] of the types of the args to
 * pass to the method (optional, if left out, the types will be derived by
 * calling getClass() on each of the arguments).</li>
 * </ul>
 * <br>
 * The following keys can also be used at need:
 * <ul>
 * <li><code>INITIAL_CONTEXT_FACTORY</code> - the context factory used to build
 * the context.</li>
 * <li><code>PROVIDER_URL</code> - the name of the environment property for
 * specifying configuration information for the service provider to use.</li>
 * </ul>
 * 
 * <p>
 * The result of the EJB method invocation will be available to
 * <code>Job/TriggerListener</code>s via
 * <code>{@link org.quartz.JobExecutionContext#getResult()}</code>.
 * </p>
 * 
 * @author hhuynh
 * {@link EJBInvokerJob}
 */
public class EJB3InvokerJob extends EJBInvokerJob {

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();

        String ejb = dataMap.getString(EJB_JNDI_NAME_KEY);
        String method = dataMap.getString(EJB_METHOD_KEY);

        Object[] arguments = (Object[]) dataMap.get(EJB_ARGS_KEY);
        if (arguments == null) {
            arguments = new Object[0];
        }
        if (ejb == null) {
            throw new JobExecutionException("must specify EJB_JNDI_NAME_KEY");
        }
        if (method == null) {
            throw new JobExecutionException("must specify EJB_METHOD_KEY");
        }

        InitialContext jndiContext = null;
        Object value = null;
        try {
            try {
                jndiContext = getInitialContext(dataMap);
                value = jndiContext.lookup(ejb);
            } catch (NamingException ne) {
                throw new JobExecutionException(ne);
            }
            
            Class<?>[] argTypes = (Class[]) dataMap.get(EJB_ARG_TYPES_KEY);
            if (argTypes == null) {
                argTypes = new Class[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    argTypes[i] = arguments[i].getClass();
                }
            }

            try {
                Object returnValue = value.getClass()
                        .getMethod(method, argTypes).invoke(value, arguments);
                context.setResult(returnValue);
            } catch (IllegalAccessException iae) {
                throw new JobExecutionException(iae);
            } catch (InvocationTargetException ite) {
                throw new JobExecutionException(ite.getTargetException());
            } catch (NoSuchMethodException nsme) {
                throw new JobExecutionException(nsme);
            }
        } finally {
            if (jndiContext != null) {
                try {
                    jndiContext.close();
                } catch (Exception e) {
                    // ignored
                }
            }
        }
    }
}
