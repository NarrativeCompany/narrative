import * as React from 'react';
import { notification } from 'antd';
import { IconType, NotificationPlacement } from 'antd/lib/notification';
import { logException } from '@narrative/shared';

export function createInvalidFieldsNotificationOptions(/*error: Error*/): NotificationOptions {
// currently we do nothing with the error - maybe down the road we give more detail.
  return {
    description: 'Form input is invalid.'
  };
}

function logError(error: Error) {
  logException('Operation failed', error);
}

// NotificationOptions is an Optional ArgsProps from 'antd/lib/notification'
export interface NotificationOptions {
  message?: React.ReactNode;
  description?: React.ReactNode;
  btn?: React.ReactNode;
  key?: string;
  onClose?: () => void;
  duration?: number | null;
  icon?: React.ReactNode;
  placement?: NotificationPlacement;
  style?: React.CSSProperties;
  prefixCls?: string;
  className?: string;
  readonly type?: IconType;
}

export const openNotification = {
  createSuccess(options: NotificationOptions = {}) {
    if (notification.success) {
      notification.success({
        ...{
          message: 'Record Created',
          description: 'Record saved.',
        },
        ...options
      });
    }
  },
  createFailed(error?: Error, options: NotificationOptions = {}) {
    if (error) {
      logError(error);
    }
    if (notification.error) {
      notification.error({
        ...{
          message: 'Record Not Created',
          description: 'Failed to save.',
        },
        ...options,
        duration: 0
      });
    }
  },
  updateSuccess(options: NotificationOptions = {}) {
    if (notification.success) {
      notification.success({
        ...{
          message: 'Record Updated',
          description: 'Record saved.',
        },
        ...options
      });
    }
  },
  updateFailed(error?: Error, options: NotificationOptions = {}) {
    if (error) {
      logError(error);
    }
    if (notification.error) {
      notification.error({
        ...{
          message: 'Record Not Updated',
          description: 'Failed to save.',
        },
        ...options,
        duration: 0
      });
    }
  },
  deleteSuccess(options: NotificationOptions = {}) {
    if (notification.success) {
      notification.success({
        ...{
          message: 'Record Deleted',
          description: 'Record deleted.',
        },
        ...options
      });
    }
  },
  deleteFailed(error?: Error, options: NotificationOptions = {}) {
    if (error) {
      logError(error);
    }
    if (notification.error) {
      notification.error({
        ...{
          message: 'Record Not Deleted',
          description: 'Failed to delete.'
        },
        ...options,
        duration: 0
      });
    }
  }
};
