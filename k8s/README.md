# NotesBhej Kubernetes deployment

This deployment connects directly from the API Pod to the existing PostgreSQL
server at `10.0.1.20:5432`. It does not run PostgreSQL in Kubernetes and does
not use TLS, matching the current Compose setup.

## One-time host-side check

The database must listen on its private interface and its firewall plus
`pg_hba.conf` must permit TCP connections on `5432` from the Kubernetes worker
nodes or Pod CIDR. Do not use `host.docker.internal` for PostgreSQL.

From a worker, identify the source range the database will see. Many CNIs SNAT
Pod egress to the node IP, so permitting the worker-node subnet is commonly
what is needed. Verify before rollout:

```bash
kubectl -n notesbhej run pg-check --rm -it --restart=Never \
  --image=postgres:16 --env PGPASSWORD='your-password' -- \
  psql -h 10.0.1.20 -U spring_user -d spring_app -c 'select 1'
```

If this fails, fix host routing/firewall/`pg_hba.conf`; no Kubernetes Service,
`hostNetwork`, or `externalIP` is needed for a direct private-IP database.

## Deploy

1. Build and push the API image, then replace the `image:` value in
   `deployment.yaml`.
2. Copy `notesbhej-secrets.example.yaml` to `notesbhej-secrets.yaml`, fill in
   the values, and keep that copy out of Git.
3. Apply the resources:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/notesbhej-secrets.yaml
kubectl apply -k k8s/
kubectl -n notesbhej rollout status deployment/notesbhej-api
kubectl -n notesbhej get pods,service
```

The Service is exposed on the Kubernetes host at `127.0.0.1:30080` for a
host-installed `cloudflared` service. It is also available inside the cluster
at `notesbhej-api.notesbhej.svc.cluster.local:8080`. Configure the Cloudflare
Tunnel public hostname to use `http://127.0.0.1:30080` as its origin service.
