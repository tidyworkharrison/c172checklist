# C172 Checklist (Offline) – Cloud Build to APK

This repo lets you build an **APK entirely in your browser** (no admin rights) using **GitHub Actions**.

## Steps

1. Go to https://github.com/new and create a new **empty repository** (public or private).
2. On your new repo page, click **"Upload files"** and upload all the contents of this folder (including the `.github/workflows/android.yml` file).
3. Click **Commit** to push the files.
4. Go to **Actions** tab → choose **"Android APK (Debug)"** workflow → click **"Run workflow"**.
5. Wait for the build to finish (2–6 minutes). Open the job, scroll to **Artifacts**, and download **C172Checklist-debug-apk** → `app-debug.apk`.
6. Sideload the APK on your Samsung S24 (enable "Install unknown apps" for My Files) or upload the APK to **https://appetize.io/upload** to run it in your browser.

> If the workflow is disabled, click **"I understand my workflows, go ahead and enable them"** on the Actions tab.

## Notes
- The app is fully offline, with tabbed sections and per-tab checkboxes that reset on tab change. JSON checklist at `app/src/main/assets/checklist.json`.
- For live device install, you can also drag the APK into an Android emulator in your browser (Appetize.io).

**Safety Reminder:** This checklist is a training/reference aid only. Always follow your exact aircraft POH/AFM and club SOPs.
