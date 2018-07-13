try { pip install -r requirements.txt }
catch { "pip is not installed!
Please install it via https://www.python.org/downloads/
" }

try { python test_dataset.py }
catch {
	"python is not installed!
Please install it via https://www.python.org/downloads/
"
}


Read-Host -Prompt "Press Enter to exit"

