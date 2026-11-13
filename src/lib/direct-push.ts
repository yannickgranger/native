// The direct connection (Android): a foreground service keeps every push
// account's JMAP event stream open and hands a StateChange to the same push
// task FCM and UnifiedPush wake (BulwarkDirectPushService.kt). Nothing is
// registered on the server or with a relay. The service holds the stream
// credentials in memory only; it gets them from the headless task in
// direct-push-task.ts.

import { NativeModules } from 'react-native';
import { supportsDirectPush } from './platform-capabilities';

export interface DirectPushAccount {
  // The local account id (username@host).
  id: string;
  // The session's eventSourceUrl template; the service fills it in.
  eventSourceUrl: string;
  authorization: string;
  types: string[];
  // The login's username, which the push task matches accounts by.
  label: string;
}

// The service's notification and channel, in the app's language, and the
// notices that it stopped itself or gave up on an account, posted in the
// channel of sync problems.
export interface DirectPushTexts {
  channelName: string;
  title: string;
  text: string;
  stoppedText: string;
  problemChannelName: string;
  givenUpText: string;
}

// The credentials task's answer to the service's request number `request`.
// `unreadable` the ids of the accounts that could not be read now (the
// service keeps their streams), `refused` those whose server refused the
// credentials. The service closes every stream whose account is in neither
// `accounts` nor `unreadable`.
export interface DirectPushAnswer {
  request: number;
  accounts: DirectPushAccount[];
  unreadable: string[];
  refused: string[];
}

// The headless task the service runs to ask for the credentials
// (DirectPushCredentialsTask.kt); its one argument is the request number.
export const DIRECT_PUSH_CREDENTIALS_TASK = 'BulwarkDirectPushCredentialsTask';

// BulwarkDirectPushModule.kt rejects any other shape.
type BulwarkDirectPushNative = {
  start(texts: DirectPushTexts): Promise<void>;
  stop(): Promise<void>;
  setAccounts(answer: DirectPushAnswer): Promise<void>;
};

export function getDirectPushNative(): BulwarkDirectPushNative | null {
  if (!supportsDirectPush) return null;
  return (NativeModules as Record<string, unknown>).BulwarkDirectPush as BulwarkDirectPushNative | undefined ?? null;
}

export function isDirectPushSupported(): boolean {
  return getDirectPushNative() !== null;
}

export function directPushTexts(tr: (key: string, fallback: string) => string): DirectPushTexts {
  return {
    channelName: tr('settings.notifications.push.direct_channel', 'Direct connection'),
    title: tr('settings.notifications.push.direct_notification_title', 'Waiting for new mail'),
    text: tr('settings.notifications.push.direct_notification_text', 'Connected directly to your mail server'),
    stoppedText: tr(
      'settings.notifications.push.direct_stopped_text',
      'New-mail notifications over the direct connection have stopped. Open the app to start them again.',
    ),
    problemChannelName: tr('device_sync.problem_channel', 'Sync problems'),
    givenUpText: tr(
      'settings.notifications.push.direct_given_up_text',
      'One of your accounts no longer gets new-mail notifications over the direct connection. Open the app to check it.',
    ),
  };
}

/** Starts the service, or makes a running one ask for the accounts again. */
export async function startDirectPush(texts: DirectPushTexts): Promise<void> {
  await getDirectPushNative()?.start(texts);
}

/** Stops the service and keeps it off after a reboot. A no-op when it is not running. */
export async function stopDirectPush(): Promise<void> {
  await getDirectPushNative()?.stop();
}
