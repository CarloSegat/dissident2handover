export async function fetchEvents() {
    try {
        const response = await fetch('http://127.0.0.1:8000/events');
        
        // Check if the response is successful
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

    } catch (error) {
        console.error('Error fetching data:', error);
    }
}