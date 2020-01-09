const manageTranslations = require('react-intl-translations-manager').default;

manageTranslations({
  messagesDirectory: 'src/shared/i18n/extracted/',
  translationsDirectory: 'src/shared/i18n/translations/',
  languages: ['es']
});
