declare global {
  namespace NodeJS {
    interface ProcessEnv {
      API_KEY: string;
      PORT?: string;
    }
  }
}
export {};
