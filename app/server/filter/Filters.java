package server.filter;

import play.http.DefaultHttpFilters;

import javax.inject.Inject;

public class Filters extends DefaultHttpFilters {
    @Inject
    public Filters(CorsFilter cors) {
        super(cors);
    }
}