import { PublicationRole } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { PublicationPlanTypeHelper } from './publicationPlanType';
import { PublicationDetailsMessages } from '../../shared/i18n/PublicationDetailsMessages';
import MessageDescriptor = ReactIntl.FormattedMessage.MessageDescriptor;
import { PublicationRoleBooleans } from '../utils/publicationRoleUtils';
type RoleBooleanSetterFunction = (roles: PublicationRoleBooleans) => void;
type RoleBooleanGetterFunction = (roles: PublicationRoleBooleans) => boolean;

// jw: let's define the PublicationRoleHelper that will provide all the extra helper logic for PublicationRoles
export class PublicationRoleHelper {
  role: PublicationRole;
  roleBooleanSetter: RoleBooleanSetterFunction;
  roleBooleanGetter: RoleBooleanGetterFunction;
  name: MessageDescriptor;
  pluralName: MessageDescriptor;
  nameWithArticle: MessageDescriptor;
  description: MessageDescriptor;

  constructor(
    role: PublicationRole,
    roleBooleanSetter: RoleBooleanSetterFunction,
    roleBooleanGetter: RoleBooleanGetterFunction,
    name: MessageDescriptor,
    pluralName: MessageDescriptor,
    nameWithArticle: MessageDescriptor,
    description: MessageDescriptor,
  ) {
    this.role = role;
    this.roleBooleanSetter = roleBooleanSetter;
    this.roleBooleanGetter = roleBooleanGetter;
    this.name = name;
    this.pluralName = pluralName;
    this.nameWithArticle = nameWithArticle;
    this.description = description;
  }

  isAdmin() {
    return this.role === PublicationRole.ADMIN;
  }

  isEditor() {
    return this.role === PublicationRole.EDITOR;
  }

  isWriter() {
    return this.role === PublicationRole.WRITER;
  }

  setRoleBoolean(roles: PublicationRoleBooleans) {
    this.roleBooleanSetter(roles);
  }

  isRole(roles: PublicationRoleBooleans): boolean {
    return this.roleBooleanGetter(roles);
  }

  getPlanLimit(planHelper: PublicationPlanTypeHelper): number {
    switch (this.role)
    {
      case PublicationRole.ADMIN:
        /* todo: error-handling: Report back to server that something is trying to get an admin role limit,
           which should never happen */
        return 0;
      case PublicationRole.EDITOR:
        return planHelper.maxEditors;
      case PublicationRole.WRITER:
        return planHelper.maxWriters;
      default:
        throw new Error('Invalid Role');
    }
  }
}

// jw: next: lets create the lookup of PublicationRole to helper object

const helpers: {[key: number]: PublicationRoleHelper} = [];
// jw: make sure to register these in the order you want them to display. (though right now we never iterate over them)
helpers[PublicationRole.ADMIN] = new PublicationRoleHelper(
  PublicationRole.ADMIN,
  roles => roles.admin = true,
  roles => roles.admin,
  PublicationDetailsMessages.PublicationRoleAdministrator,
  PublicationDetailsMessages.PublicationRoleAdministrators,
  PublicationDetailsMessages.PublicationRoleAdministratorWithArticle,
  PublicationDetailsMessages.PublicationRoleAdministratorDescription,
);
helpers[PublicationRole.EDITOR] = new PublicationRoleHelper(
  PublicationRole.EDITOR,
  roles => roles.editor = true,
  roles => roles.editor,
  PublicationDetailsMessages.PublicationRoleEditor,
  PublicationDetailsMessages.PublicationRoleEditors,
  PublicationDetailsMessages.PublicationRoleEditorWithArticle,
  PublicationDetailsMessages.PublicationRoleEditorDescription,
);
helpers[PublicationRole.WRITER] = new PublicationRoleHelper(
  PublicationRole.WRITER,
  roles => roles.writer = true,
  roles => roles.writer,
  PublicationDetailsMessages.PublicationRoleWriter,
  PublicationDetailsMessages.PublicationRoleWriters,
  PublicationDetailsMessages.PublicationRoleWriterWithArticle,
  PublicationDetailsMessages.PublicationRoleWriterDescription,
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedPublicationRole = new EnumEnhancer<PublicationRole, PublicationRoleHelper>(
  helpers
);
