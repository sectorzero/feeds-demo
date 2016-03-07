package org.sectorzero.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang.Validate;
import org.sectorzero.core.articles.ArticleRef;
import org.sectorzero.core.feeds.FeedViewAggregator;
import org.sectorzero.core.feeds.FeedsView;
import org.sectorzero.model.Feed;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/aggregate")
@Api(value = "/aggregate", description = "Demos an API which aggregates articles from a list of feeds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AggregateResource {

  final FeedsView feedsView;
  final FeedViewAggregator aggregator;

  @Inject
  public AggregateResource(FeedsView feedsView, FeedViewAggregator aggregator) {
    this.feedsView = feedsView;
    this.aggregator = aggregator;
  }

  @GET
  @ApiOperation(
      value = "Shows the aggregated articles from the list of feeds provided",
      notes = "Ignores feeds which are not existent",
      response = Feed.class)
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Invalid Message ( dummy for demo )"),
      @ApiResponse(code = 404, message = "Not Found ( dummy for demo )") })
  @Timed
  public List<ArticleRef> listFeed(
      @ApiParam(value = "Set of feeds to be used to aggregate the user view", required = true, allowMultiple = true)
      @QueryParam("feedname") List<String> feedsList,
      @ApiParam(value = "Number of Entries Required", required = false)
      @QueryParam("numEntries") @DefaultValue("10") Integer numEntries) {

    Validate.notNull(feedsList);

    return aggregator.aggregate(
        feedsList.stream()
            .filter(f -> feedsView.getConfig().getFeeds().contains(f))
            .map(f -> feedsView.getFeedViewStore().getArticlesForFeed(f))
            .collect(Collectors.toList()),
        numEntries);
  }
}
