package csp;

import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import static play.Play.application;

public class ContentSecurityPolicyAction extends Action<ContentSecurityPolicy> {

    public static final String OFFICIAL = "Content-Security-Policy";
    public static final String MOZILLA = "X-Content-Security-Policy";
    public static final String WEBKIT = "X-WebKit-CSP";

    public static final String POLICIES = ContentSecurityPolicyAction.getPolicies();

    public static final String POLICY_ALREADY_SET = "policy-set";

    @Override
    public Result call(Http.Context ctx) throws Throwable {
        if(!ctx.args.containsKey(POLICY_ALREADY_SET)) {
            ctx.args.put(POLICY_ALREADY_SET, "");

            String value = configuration.value();
            String csp = (value == null || value.isEmpty()) ? POLICIES : value;

            ctx.response().setHeader(OFFICIAL, csp);
            ctx.response().setHeader(MOZILLA, csp);
            ctx.response().setHeader(WEBKIT, csp);
        }

        return delegate.call(ctx);
    }

    private static String getPolicies() {
        String policy = application().configuration().getString("csp.policy");
        Logger.debug(OFFICIAL + ": " + policy);
        return policy;
    }
}