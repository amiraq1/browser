# Nabd Browser — Download Manager

## 1. Problem

Nabd Browser currently does not have a clear download management flow. Users need a predictable, private way to:

- Download files from WebView.
- Know the file name and file size when available.
- Save files in Downloads.
- Open a download after it completes.
- See a list of downloads.
- Keep download handling local-only without weakening privacy.

## 2. Goals

- Support WebView downloads through `DownloadListener`.
- Save files using the public Downloads folder or app-specific storage depending on Android version and platform rules.
- Show a Toast or notification when a download starts and when it completes.
- Add `DownloadsActivity` to display downloads.
- Open downloaded files through a safe `Intent`.
- Add a Downloads entry in the main menu or Settings.
- Preserve Nabd Browser privacy principles: no telemetry, no remote logging, local-only behavior.

## 3. Non-goals

- No download acceleration.
- No multi-thread downloading.
- No complex background service in this phase.
- No cloud sync.
- No upload feature.
- No in-app file preview.
- No unnecessary dangerous permissions.

## 4. Privacy/Security

- Do not send download URLs to any server.
- Do not add telemetry or analytics.
- Prefer Android's official `DownloadManager` when possible.
- Verify or preserve MIME type when available from WebView/server headers.
- Open files through `FileProvider` if direct file access is not safe for the Android version.
- Never execute downloaded files automatically.
- Add a simple warning for risky file types later, especially APK/executable-like files.
- Do not log sensitive URLs, cookies, headers, or local file paths.
- Keep cookies/headers limited to what `DownloadManager` needs to complete the user-requested download.

## 5. UX

When the user taps a download link:

- The download starts directly or shows a simple confirmation if the file looks risky or unclear.
- Show Toast: `Download started`.

When the download completes:

- Show a Toast or system notification, depending on platform support and permission state.

Downloads screen:

- Show a list of downloaded files.
- Display name.
- Display size when available.
- Display date.
- Display status.
- Provide an Open button.
- Provide a Delete from list action now, with Delete file as a later option.

Empty state:

- `No downloads yet`

## 6. Android Compatibility

- Use Android `DownloadManager` as the first implementation path.
- Respect scoped storage rules.
- Support Android 10+ behavior without relying on legacy external storage access.
- Avoid `WRITE_EXTERNAL_STORAGE` on modern devices when possible.
- Handle Android 13+ notification permission later if completion notifications need it.
- Fall back to Toasts when notifications are unavailable or not permitted.

## 7. Proposed Files

- `downloads/DownloadItem.kt`
- `downloads/DownloadRepository.kt` or Room Entity/Dao if persistence requires it later.
- `ui/DownloadsActivity.kt`
- `res/layout/activity_downloads.xml`
- `res/layout/item_download.xml`
- Update `AndroidManifest.xml`.
- Update `MainActivity.kt` to add `DownloadListener`.
- Update `main_menu.xml` to add Downloads.
- Update `strings.xml`.

## 8. Implementation Plan

Step 1: Wire WebView `DownloadListener` + Android `DownloadManager`.

- Capture `url`, `userAgent`, `contentDisposition`, `mimeType`, and `contentLength`.
- Use a safe filename derived from platform helpers such as `URLUtil.guessFileName`.
- Enqueue the request through `DownloadManager`.
- Show `Download started`.
- Store a minimal local record with download id, name, status, date, and destination info when safe.

Step 2: Add a simple `DownloadsActivity`.

- Show local download records.
- Support empty state.
- Query `DownloadManager` for status when possible.
- Keep the UI small and reliable.

Step 3: Add menu entry.

- Add Downloads to the main menu or Settings.
- Open `DownloadsActivity` from that entry.

Step 4: Open downloaded files safely.

- Use MIME-aware `Intent.ACTION_VIEW`.
- Use `FileProvider` or `content://` URI when required.
- Grant temporary read permission.
- Show a safe error message if no app can open the file.
- Do not auto-open downloads.

Step 5: QA.

- Test common file types.
- Test empty state.
- Test unsupported file opening.
- Confirm no telemetry or remote calls were introduced.
- Confirm no unnecessary permissions were added.

## 9. Acceptance Tests

- Download a small PDF.
- Download an image.
- Download an APK or ZIP and confirm it does not open automatically.
- Downloads screen opens.
- Empty state works.
- Opening a downloaded file works or shows a safe message.
- No crash during download start, completion, list display, or open action.
- Build successful.
- No telemetry.

## 10. Risks

- Storage permission differences across Android versions.
- Scoped storage behavior may change destination visibility.
- Unsafe or malformed file names.
- Incorrect or missing MIME type from the server.
- WebView download URLs may require headers or cookies.
- Downloads triggered through POST requests are outside the first phase.
- `blob:` URLs are outside the first phase.
- Some sites may provide misleading file extensions or content types.
- System notification behavior depends on Android version and notification permission state.

## 11. Decision

Start simple:

- Android `DownloadManager`.
- Simple local list.
- No complex background service.
- No download acceleration.
- No extra dependencies.
- No telemetry.

Scraping, bypass, cloud sync, and advanced download orchestration are intentionally out of scope for this phase.
