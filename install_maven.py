import urllib.request
import zipfile
from pathlib import Path

zip_path = Path(r'D:/Student-moral-education-score-management-system/apache-maven-3.9.9-bin.zip')
extract_dir = Path(r'D:/Student-moral-education-score-management-system/apache-maven-3.9.9')
url = 'https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip'

if not zip_path.exists():
    print('Downloading Maven...')
    urllib.request.urlretrieve(url, zip_path)
    print('Downloaded:', zip_path)
else:
    print('Zip already exists:', zip_path)

if not extract_dir.exists():
    print('Extracting Maven...')
    with zipfile.ZipFile(zip_path, 'r') as zf:
        zf.extractall(path=zip_path.parent)
    print('Extracted to:', extract_dir)
else:
    print('Already extracted:', extract_dir)
