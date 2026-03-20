import requests

BASE_URL = "http://localhost:8080/api/v1/files"


def get_upload_url(file_name):
    params = {
        "fileName": file_name,
        "fileType": "text/plain"
    }

    r = requests.post(f"{BASE_URL}/upload-url", params=params)

    if r.status_code != 200:
        raise Exception(f"Failed to get upload URL: {r.text}")

    data = r.json()
    print("Upload URL response:", data)
    return data


def upload_file(upload_url, content: bytes):
    headers = {
        "Content-Type": "text/plain"
    }

    r = requests.put(upload_url, data=content, headers=headers)

    if r.status_code not in (200, 201):
        raise Exception(f"Upload failed: {r.status_code} {r.text}")

    print("Upload successful")


def download_file(download_url):
    r = requests.get(download_url)

    if r.status_code != 200:
        raise Exception(f"Download failed: {r.status_code}")

    print("Downloaded content:", r.content.decode())
    return r.content


def main():
    file_name = "test.txt"
    content = b"hello from python upload test"

    # 1. Get presigned URL
    res = get_upload_url(file_name)

    # adjust keys based on your DTO
    upload_url = res["signedURL"]
    download_url = res["publicFileUrl"]

    # 2. Upload
    upload_file(upload_url, content)

    # 3. Download (if available)
    if download_url:
        download_file(download_url)
    else:
        print("No download URL returned. Check S3 manually.")


if __name__ == "__main__":
    main()