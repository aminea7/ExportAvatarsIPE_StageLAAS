package org.eclipse.om2m.Avatars;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.interworking.service.InterworkingService;
//import org.eclipse.om2m.ipe.sample.controller.LifeCycleManager;
//import org.eclipse.om2m.ipe.sample.controller.SampleController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
//import org.eclipse.om2m.Avatars.Repository;

/**
 *  Manages the starting and stopping of the bundle.
 */
public class Activator implements BundleActivator {
	

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}
	
    /** Logger */
    private static Log logger = LogFactory.getLog(Activator.class);
    /** SCL service tracker */
    private ServiceTracker<Object, Object> cseServiceTracker;


    @Override
    public void start(BundleContext bundleContext) throws Exception {
    	
    	Activator.context = bundleContext;
		System.out.println("IPE AVATARS ACTIVATOR START ");
    	
		//s'enregistre en tant que service ds le ctxt
        logger.info("Register IpeService for IPE AVATARS..");
        bundleContext.registerService(InterworkingService.class.getName(), new SampleRouter(), null);
        logger.info("IpeService is registered for IPE AVATARS.");


        
        //Ecoute sur ce ctxt, dès que nv service cse
        cseServiceTracker = new ServiceTracker<Object, Object>(bundleContext, CseService.class.getName(), null) {
            public void removedService(ServiceReference<Object> reference, Object service) {
                logger.info("CseService removed");
            }

            public Object addingService(ServiceReference<Object> reference) {
                logger.info("CseService discovered");
                CseService cseService = (CseService) this.context.getService(reference);
                //SampleController.setCse(cseService);
               /* new Thread(){
                    public void run(){
                    	
                    }
                }.start();
                */
                //TBD: Correct this
                

                return cseService;
            }
        };
        cseServiceTracker.open();
        
        System.out.println("I'M IN THE THREAAAAAADDDD !!!");
    	try{    	
    	}
    	catch(Exception e){
            logger.error("[Repo Error]", e);

    	}
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    	
    	//Activator.context = null;
		System.out.println("IPE AVATARS ACTIVATOR STOP ");
    	
        logger.info("Stop Ipe Avatars");
        /*
        try {
        	LifeCycleManager.stop();
        } catch (Exception e) {
            logger.error("Stop IPE Sample error", e);
        }*/
    }

    
}
