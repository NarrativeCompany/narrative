import { FormattedMessage } from 'react-intl';
import { SearchType } from '@narrative/shared';
import { SearchMessages } from '../i18n/SearchMessages';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';

// jw: let's define the SearchTypeHelper that will provide all the extra helper logic for SearchTypes
export class SearchTypeHelper {
  type: SearchType;
  titleMessage: FormattedMessage.MessageDescriptor;

  constructor(
    type: SearchType,
    titleMessage: FormattedMessage.MessageDescriptor
  ) {
    this.type = type;
    this.titleMessage = titleMessage;
  }

  isEverything() {
    return this.type === SearchType.everything;
  }

  getUrlParamValue() {
    if (this.isEverything()) {
      return undefined;
    }

    return this.type;
  }
}

// jw: next: lets create the lookup of SearchType to helper object

const searchTypeHelpers: {[key: number]: SearchTypeHelper} = [];
// jw: make sure to register these in the order you want them to display.
searchTypeHelpers[SearchType.everything] = new SearchTypeHelper(
  SearchType.everything,
  SearchMessages.Title_Everything
);
searchTypeHelpers[SearchType.posts] = new SearchTypeHelper(
  SearchType.posts,
  SearchMessages.Title_Posts
);
searchTypeHelpers[SearchType.niches] = new SearchTypeHelper(
  SearchType.niches,
  SearchMessages.Title_Niches
);
searchTypeHelpers[SearchType.publications] = new SearchTypeHelper(
  SearchType.publications,
  SearchMessages.Title_Publications
);
searchTypeHelpers[SearchType.members] = new SearchTypeHelper(
  SearchType.members,
  SearchMessages.Title_Members
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedSearchType = new EnumEnhancer<SearchType, SearchTypeHelper>(
  searchTypeHelpers
);
