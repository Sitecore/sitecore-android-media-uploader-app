package net.sitecore.android.mediauploader.model;

import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

public class Address implements android.os.Parcelable {
    public final String address;
    public final String countryCode;
    public final String zipCode;
    public final LatLng latLng;

    public Address(String address, String countryCode, String zipCode, LatLng latLng) {
        this.address = address;
        this.countryCode = countryCode;
        this.zipCode = zipCode;
        this.latLng = latLng;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(countryCode);
        dest.writeString(zipCode);
        dest.writeParcelable(latLng, flags);
    }

    private Address(Parcel in) {
        this.address = in.readString();
        this.countryCode = in.readString();
        this.zipCode = in.readString();
        this.latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static Creator<Address> CREATOR = new Creator<Address>() {
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }

        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
}
