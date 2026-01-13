import { Link } from 'react-router-dom'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'

export function NotFoundPage() {
  return (
    <div className="mx-auto max-w-xl">
      <Card title="Страница не найдена" subtitle="Похоже, такого маршрута нет.">
        <div className="flex items-center gap-2">
          <Link to="/">
            <Button>На главную</Button>
          </Link>
          <Link to="/account">
            <Button variant="secondary">Аккаунт</Button>
          </Link>
        </div>
      </Card>
    </div>
  )
}


