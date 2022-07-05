package org.drinklink.app.model;

import android.text.TextUtils;

import org.drinklink.app.dependency.ApplicationModule;
import org.drinklink.app.persistence.model.GsonExclude;
import org.drinklink.app.persistence.model.Internal;
import org.drinklink.app.utils.ListUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Place extends NamedObject {

    public static final Place EMPTY_PLACE = new Place() {
        @Override
        public boolean isUserSelected() {
            return false;
        }
    };

    @Deprecated
    @Internal
    public boolean userSelected = true;

    public String address;

    public String description;

    public double averageRate;

    public String code;

    public boolean isTableDeliveryEnabled;

    public Boolean isPickupEnabled;

    //public Area area;
    public String area;

    // This is redundant, we can probably go without it.
    //public City city;
    public String city;

    public List<Table> tables;

    public List<Bar> bars;

    public List<Discount> discounts;

    @GsonExclude
    public List<PlaceReview> reviews;

    @GsonExclude
    public List<DrinkCategory> menu;

    public List<Bartender> bartenders;

    @GsonExclude
    public List<WorkHours> workHours;

    private String timeZoneId;

    private BigDecimal vipOrderCharge = BigDecimal.ZERO;

    private long expectedOrderPreparationTime;

    private long timeToCollect;

    private String coverImagePath;

    private boolean isServiceChargeEnabled;

    private BigDecimal serviceChargePercentage = BigDecimal.ZERO;

    public Bar getBar(Integer barId) {
        return ListUtil.findFirst(getBars(), item -> barId != null && item.getId() == barId);
    }

    public String getCoverImageUrl() {
        if (!TextUtils.isEmpty(coverImagePath) && !coverImagePath.startsWith("http")) {
            return prePendBaseUri();
        }
        return coverImagePath;
    }

    @NotNull
    private String prePendBaseUri() {
        return (coverImagePath.startsWith("/") ? ApplicationModule.BASE_URL_WITHOUT_SLASH : ApplicationModule.BASE_URL_WITHOUT_SLASH) + coverImagePath;
    }

    public long getTimeToCollect() {
        return timeToCollect;
    }

    public boolean getIsPickupEnabled() {
        return Boolean.FALSE.equals(isPickupEnabled) ? false : true;
    }
}
