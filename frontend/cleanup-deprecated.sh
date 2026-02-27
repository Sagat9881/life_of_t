#!/bin/bash
# Script to remove deprecated api/hooks files

echo "Removing deprecated api/hooks files..."

rm -f src/api/hooks/useActions.ts
rm -f src/api/hooks/useGameState.ts

echo "Done! Deprecated hooks removed."
echo "All API logic is now in:"
echo "  - src/store/gameStore.ts"
echo "  - src/services/api.ts"
