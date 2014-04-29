package net.sitecore.android.mediauploader.model;

import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

public class Address implements android.os.Parcelable {
    public final String address;
    public final LatLng latLng;

    public Address(String address, LatLng latLng) {
        this.address = address;
        this.latLng = latLng;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeParcelable(this.latLng, flags);
    }

    public Address(Parcel in) {
        this.address = in.readString();
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
