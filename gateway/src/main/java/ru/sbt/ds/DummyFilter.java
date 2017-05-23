package ru.sbt.ds;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created on 23.05.2017.
 *
 * @author Kirill M. Korotkov
 */
public class DummyFilter extends ZuulFilter {

    @Autowired
    EurekaClient client;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        List<Application> apps = client.getApplications()
                .getRegisteredApplications();
        InstanceInfo last;
        for (Application app:
             apps) {
            if (app.getName().equals("PhotoServiceClient")) {
                List<InstanceInfo> infs = app.getInstances();
                last = infs.get(0);
                for (InstanceInfo info:
                     infs) {
                    int pos = info.getMetadata().get("version")
                            .compareTo(last.getMetadata().get("version"));
                    if (pos > 0) {
                        last = info;
                    }
                }
                try {
                    ctx.setRouteHost(new URL("http://" + last.getIPAddr() + ":" + last
                            .getPort()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
