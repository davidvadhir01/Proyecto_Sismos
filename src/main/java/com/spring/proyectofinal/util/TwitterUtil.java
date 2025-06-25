package com.spring.proyectofinal.util;

import java.util.List;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.springframework.stereotype.Component;

@Component
public class TwitterUtil {

    public Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("HaKVPj7XqtG3PGHa3jeCMSvHK")
          .setOAuthConsumerSecret("b6nhNsrZaGECdeSn7id4RviQvZSYdLbm7oRrqHDdfGTtQfAW7q")
          .setOAuthAccessToken("1553599912834744321-YNQatxPYyTzm3J3QxX4UkgZ9yiVC8t")
          .setOAuthAccessTokenSecret("A7O7by8io9ialnNKtgFGymhZz6744hafi9z0mAZz9ijaD");

        return new TwitterFactory(cb.build()).getInstance();
    }

    //API key: HaKVPj7XqtG3PGHa3jeCMSvHK    
    //API and secret: b6nhNsrZaGECdeSn7id4RviQvZSYdLbm7oRrqHDdfGTtQfAW7q    
    //Bearer token: AAAAAAAAAAAAAAAAAAAAAEDt2gEAAAAAcSF3BU7VEoI6%2F0jeXQP1M1MgYos%3DJba8KCCJZhHUC8O6Y8Ow7WngzgdcKJeVohCQNCLFWKya9qXDjz
    //Access token: 1553599912834744321-YNQatxPYyTzm3J3QxX4UkgZ9yiVC8t
    //Access token secret: A7O7by8io9ialnNKtgFGymhZz6744hafi9z0mAZz9ijaD

    public List<Status> getLatestTweets(String screenName, int count) throws TwitterException {
        return getTwitterInstance().getUserTimeline(screenName, new Paging(1, count));
    }
}