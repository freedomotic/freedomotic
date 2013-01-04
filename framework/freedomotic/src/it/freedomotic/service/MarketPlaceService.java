package it.freedomotic.service;

import it.freedomotic.app.Freedomotic;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Gabriel Pulido
 */
public class MarketPlaceService {

    private static MarketPlaceService service;
    private Lookup marketPlaceLookup;
    private Collection<? extends IMarketPlace> marketPlaces;
    private Template marketPlaceTemplate;
    private Result marketPlaceResults;

    /**
     * Creates a new instance of DictionaryService
     */
    private MarketPlaceService() {
        try {
        marketPlaceLookup = Lookup.getDefault();
        marketPlaceTemplate = new Template(IMarketPlace.class);
        marketPlaceResults = marketPlaceLookup.lookup(marketPlaceTemplate);
        marketPlaces = marketPlaceResults.allInstances();
        marketPlaceResults.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent e) {
                Freedomotic.logger.severe("Lookup has changed");
            }
        });
        }catch (Exception e) {
            Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
        }
    }

    public static synchronized MarketPlaceService getInstance() {
        if (service == null) {
            service = new MarketPlaceService();
        }
        return service;
    }

    public ArrayList<IPluginPackage> getPackageList() {
        ArrayList<IPluginPackage> packageList = null;
        try {
            packageList = new ArrayList<IPluginPackage>();
            for (IMarketPlace market : marketPlaces) {
                packageList.addAll(market.getAvailablePackages());
            }
        } catch (Exception e) {
            Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
        }
        return packageList;
    }
    
    public ArrayList<IPluginPackage> getPackageList(IPluginCategory category) {
        ArrayList<IPluginPackage> packageList = null;
        try {
            packageList = new ArrayList<IPluginPackage>();
            for (IMarketPlace market : marketPlaces) {
                packageList.addAll(market.getAvailablePackages(category));
            }
        } catch (Exception e) {
            Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
        }
        return packageList;
    }
    
    public ArrayList<IPluginCategory> getCategoryList() {
        ArrayList<IPluginCategory> categoryList = null;
        try {
            categoryList = new ArrayList<IPluginCategory>();
            for (IMarketPlace market : marketPlaces) {
                categoryList.addAll(market.getAvailableCategories());
            }
        } catch (Exception e) {
            Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
        }
        return categoryList;
    }
    
    
    
}
