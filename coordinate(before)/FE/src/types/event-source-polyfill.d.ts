declare module 'event-source-polyfill' {
  // EventSourceInit 타입 확장
  interface EventSourceInit {
    headers?: Record<string, string>;
    withCredentials?: boolean;
    timeout?: number; // 타임아웃 설정 (밀리초)
    heartbeatTimeout?: number;
  }

  export class EventSourcePolyfill implements EventSource {
    constructor(url: string, eventSourceInitDict?: EventSourceInit);
    readonly CLOSED: 2;
    readonly CONNECTING: 0;
    readonly OPEN: 1;
    readonly readyState: number;
    readonly url: string;
    readonly withCredentials: boolean;
    onopen: ((this: EventSource, ev: Event) => any) | null;
    onmessage: ((this: EventSource, ev: MessageEvent) => any) | null;
    onerror: ((this: EventSource, ev: Event) => any) | null;
    addEventListener(type: string, listener: EventListener): void;
    removeEventListener(type: string, listener: EventListener): void;
    dispatchEvent(evt: Event): boolean;
    close(): void;
  }

  export class NativeEventSource implements EventSource {
    constructor(url: string, eventSourceInitDict?: EventSourceInit);
    readonly CLOSED: 2;
    readonly CONNECTING: 0;
    readonly OPEN: 1;
    readonly readyState: number;
    readonly url: string;
    readonly withCredentials: boolean;
    onopen: ((this: EventSource, ev: Event) => any) | null;
    onmessage: ((this: EventSource, ev: MessageEvent) => any) | null;
    onerror: ((this: EventSource, ev: Event) => any) | null;
    addEventListener(type: string, listener: EventListener): void;
    removeEventListener(type: string, listener: EventListener): void;
    dispatchEvent(evt: Event): boolean;
    close(): void;
  }
}
