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
 * @author GGPT
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

    }

    public static synchronized MarketPlaceService getInstance() {
        if (service == null) {
            service = new MarketPlaceService();
        }
        return service;
    }

    public ArrayList<PluginPackage> getPackageList() {
        ArrayList<PluginPackage> packageList = new ArrayList<PluginPackage>();
        for (IMarketPlace market : marketPlaces) {
            packageList.addAll(market.getAvailablePackages());
        }
        return packageList;
    }
}
