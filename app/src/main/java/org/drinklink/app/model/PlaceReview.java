package org.drinklink.app.model;

import lombok.Data;

@Data
public class PlaceReview {

        public String title;

        public String description;

        /// <summary>
        /// Rate [1-5].
        /// </summary>
        public int rate;

        public Place place;

        public Customer customer;
}
