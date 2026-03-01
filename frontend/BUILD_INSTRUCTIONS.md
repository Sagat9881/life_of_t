# Build Instructions

## If you get TypeScript errors about Button props or Telegram types:

### Solution 1: Pull latest changes
```bash
cd frontend
git pull origin dev/0.1.0
```

### Solution 2: Clean and rebuild
```bash
cd frontend
rm -rf node_modules/.cache
rm -rf dist
npm run build
```

### Solution 3: If Button.tsx still shows old types
Check that `frontend/src/components/shared/Button/Button.tsx` contains:

```typescript
export interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'danger' | 'accent' | 'outline';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  isLoading?: boolean;
  fullWidth?: boolean;
  onClick?: () => void;
  className?: string;
  style?: React.CSSProperties;
}
```

If not, the file hasn't been pulled. Do:
```bash
git fetch origin
git reset --hard origin/dev/0.1.0
```

## Current commit on dev/0.1.0
SHA: 6a29bfb2e8ae582cec4cc4dd5293820c52dc25c3

All files should be up to date with this commit.
