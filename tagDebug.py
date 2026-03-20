import requests
import json

API = "http://localhost:8080/api/v1"

payload = {
    "name": "debug-tag"
}

r = requests.post(f"{API}/tags", json=payload)

print("STATUS:", r.status_code)
print("HEADERS:", r.headers)
print("RAW TEXT:", r.text)

try:
    print("JSON:", json.dumps(r.json(), indent=2))
except Exception as e:
    print("JSON PARSE FAILED:", e)