package com.yihu.hospital.caihongqiji.model;

/**
 * Gson 辅助类
 */
public class RoomInfoJson {
    private String uid;
    private String name;

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String subname;
    private INFO info;

    public LBS getLbs() {
        return lbs;
    }

    public INFO getInfo() {
        return info;
    }

    private LBS lbs;


    public String getHostId() {
        return uid;
    }

    public class INFO {
        private String title;
        private int roomnum;
        private String type;
        private String groupid;
        private String cover;
        private int thumbup;
        private int memsize;

        public String getTitle() {
            return title;
        }

        public int getRoomnum() {
            return roomnum;
        }

        public String getType() {
            return type;
        }

        public String getGroupid() {
            return groupid;
        }

        public String getCover() {
            return cover;
        }


        public int getThumbup() {
            return thumbup;
        }

        public int getMemsize() {
            return memsize;
        }



        @Override
        public String toString() {
            return "HOST{" +
                    "title='" + title + '\'' +
                    ", roomnum='" + roomnum + '\'' +
                    ", type='" + type + '\'' +
                    ", groupid='" + groupid + '\'' +
                    ", cover='" + cover + '\'' +
                    ", thumbup='" + thumbup + '\'' +
                    ", memsize='" + memsize + '\'' +
                    '}';
        }
    }

    public class LBS {
        private double longitude;
        private double latitue;
        private String address;

        public double getLongitude() {
            return longitude;
        }

        public double getLatitue() {
            return latitue;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return "LBS{" +
                    "longitude=" + longitude +
                    ", latitue=" + latitue +
                    ", address='" + address + '\'' +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "RoomInfoJson{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", subname='" + subname + '\'' +
                ", info=" + info +
                ", lbs=" + lbs +
                '}';
    }


}
