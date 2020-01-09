interface Enum {
  [id: number]: string;
}

interface EnhancedEnum<Enhanced> {
  [id: number]: Enhanced;
}

export class EnumEnhancer<E extends Enum, Enhanced> {
  enhancers: Enhanced[];
  private enhancedEnum: EnhancedEnum<Enhanced>;

  constructor(enhancedEnum: EnhancedEnum<Enhanced>) {
    this.enhancedEnum = enhancedEnum;
    this.enhancers = Object.values(enhancedEnum);
  }

  public get(enumKey: E): Enhanced {
    const enhanced: Enhanced = this.enhancedEnum[`${enumKey}`];

    if (!enhanced) {
      // todo:error-handling: We need to make sure this is reported properly for developers. It should be breaking!
    }

    return enhanced;
  }
}
