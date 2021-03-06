// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./message.proto
package com.example.glowybits.rcp;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoEnum;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.ENUM;
import static com.squareup.wire.Message.Datatype.FLOAT;
import static com.squareup.wire.Message.Datatype.INT32;
import static com.squareup.wire.Message.Datatype.UINT32;
import static com.squareup.wire.Message.Label.REQUIRED;

public final class ChangeSettings extends Message {

  public static final Mode DEFAULT_MODE = Mode.CHASE;
  public static final Integer DEFAULT_BRIGHTNESS = 0;
  public static final Float DEFAULT_SPEED = 0F;
  public static final Float DEFAULT_RAINBOW_SPEED = 0F;
  public static final Float DEFAULT_WIDTH = 0F;
  public static final Integer DEFAULT_COLOR1 = 0;
  public static final Integer DEFAULT_COLOR2 = 0;
  public static final Integer DEFAULT_COLOR3 = 0;

  @ProtoField(tag = 1, type = ENUM, label = REQUIRED)
  public final Mode mode;

  @ProtoField(tag = 2, type = INT32, label = REQUIRED)
  public final Integer brightness;

  @ProtoField(tag = 3, type = FLOAT, label = REQUIRED)
  public final Float speed;

  @ProtoField(tag = 4, type = FLOAT, label = REQUIRED)
  public final Float rainbow_speed;

  @ProtoField(tag = 5, type = FLOAT, label = REQUIRED)
  public final Float width;

  @ProtoField(tag = 6, type = UINT32, label = REQUIRED)
  public final Integer color1;

  @ProtoField(tag = 7, type = UINT32, label = REQUIRED)
  public final Integer color2;

  @ProtoField(tag = 8, type = UINT32, label = REQUIRED)
  public final Integer color3;

  private ChangeSettings(Builder builder) {
    super(builder);
    this.mode = builder.mode;
    this.brightness = builder.brightness;
    this.speed = builder.speed;
    this.rainbow_speed = builder.rainbow_speed;
    this.width = builder.width;
    this.color1 = builder.color1;
    this.color2 = builder.color2;
    this.color3 = builder.color3;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ChangeSettings)) return false;
    ChangeSettings o = (ChangeSettings) other;
    return equals(mode, o.mode)
        && equals(brightness, o.brightness)
        && equals(speed, o.speed)
        && equals(rainbow_speed, o.rainbow_speed)
        && equals(width, o.width)
        && equals(color1, o.color1)
        && equals(color2, o.color2)
        && equals(color3, o.color3);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = mode != null ? mode.hashCode() : 0;
      result = result * 37 + (brightness != null ? brightness.hashCode() : 0);
      result = result * 37 + (speed != null ? speed.hashCode() : 0);
      result = result * 37 + (rainbow_speed != null ? rainbow_speed.hashCode() : 0);
      result = result * 37 + (width != null ? width.hashCode() : 0);
      result = result * 37 + (color1 != null ? color1.hashCode() : 0);
      result = result * 37 + (color2 != null ? color2.hashCode() : 0);
      result = result * 37 + (color3 != null ? color3.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<ChangeSettings> {

    public Mode mode;
    public Integer brightness;
    public Float speed;
    public Float rainbow_speed;
    public Float width;
    public Integer color1;
    public Integer color2;
    public Integer color3;

    public Builder() {
    }

    public Builder(ChangeSettings message) {
      super(message);
      if (message == null) return;
      this.mode = message.mode;
      this.brightness = message.brightness;
      this.speed = message.speed;
      this.rainbow_speed = message.rainbow_speed;
      this.width = message.width;
      this.color1 = message.color1;
      this.color2 = message.color2;
      this.color3 = message.color3;
    }

    public Builder mode(Mode mode) {
      this.mode = mode;
      return this;
    }

    public Builder brightness(Integer brightness) {
      this.brightness = brightness;
      return this;
    }

    public Builder speed(Float speed) {
      this.speed = speed;
      return this;
    }

    public Builder rainbow_speed(Float rainbow_speed) {
      this.rainbow_speed = rainbow_speed;
      return this;
    }

    public Builder width(Float width) {
      this.width = width;
      return this;
    }

    public Builder color1(Integer color1) {
      this.color1 = color1;
      return this;
    }

    public Builder color2(Integer color2) {
      this.color2 = color2;
      return this;
    }

    public Builder color3(Integer color3) {
      this.color3 = color3;
      return this;
    }

    @Override
    public ChangeSettings build() {
      checkRequiredFields();
      return new ChangeSettings(this);
    }
  }

  public enum Mode {
    @ProtoEnum(0)
    CHASE,
    @ProtoEnum(1)
    STARS,
    @ProtoEnum(2)
    SPIRAL,
    @ProtoEnum(3)
    JOINT,
  }
}
