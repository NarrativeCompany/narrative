export interface ModCenterNicheSlotSettingsFormValues {
  moderatorSlots: number;
}

const modCenterNicheSlotSettingsInitialValues: ModCenterNicheSlotSettingsFormValues = {
  moderatorSlots: 1
};

export const modCenterNicheSlotSettingsUtil = {
  mapPropsToValues: (defaultValues: ModCenterNicheSlotSettingsFormValues) =>
    ({...modCenterNicheSlotSettingsInitialValues, ...defaultValues })
};
